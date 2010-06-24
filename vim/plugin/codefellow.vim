" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1

if exists("loaded_codefellow")
    finish
endif
let loaded_codefellow=1

" OmniCompletion
autocmd FileType scala setlocal omnifunc=CodeFellowComplete
autocmd FileType scala imap <buffer> <C-SPACE> <C-O>:call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowCompleteMember")<CR><C-X><C-O><C-P>
autocmd FileType scala imap <buffer> <C-S-SPACE> <C-O>:call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowCompleteType")<CR><C-X><C-O><C-P>
autocmd FileType scala map <buffer> <F1> :call CodeFellowPrintTypeInfo()<CR>

" Balloon type information
if has("balloon_eval")
    autocmd FileType scala setlocal ballooneval
    autocmd FileType scala setlocal balloondelay=300
    autocmd FileType scala setlocal balloonexpr=CodeFellowBalloonType()
endif

" Compilation on save
autocmd BufWritePost *.scala call CodeFellowReloadFile()

function s:SendMessage(type, ...)
python << endpython
try:
    import socket
    import vim
    s = socket.create_connection(("localhost", 9081))

    argsSize = int(vim.eval("a:0"))
    args = []
    for i in range(1, argsSize + 1):
        args.append(vim.eval("a:" + str(i)))

    msg = "{"
    msg += '"moduleIdentifierFile": "' + vim.eval('expand("%:p")') + '",'
    msg += '"message": "' + vim.eval("a:type") + '",'
    msg += '"arguments": [' + ",".join(map(lambda e: '"' + e + '"', args)) + ']'
    msg += "}"
    msg += "\nENDREQUEST\n"
    s.sendall(msg)

    data = ""
    while 1:
        tmp = s.recv(1024)
        if not tmp:
            break
        data += tmp

    vim.command('return "' + data + '"')
except:
    # Probably not connected
    # Stay silent to not interrupt
    vim.command('return ""')
endpython
endfunction

function s:getMousePointerOffset()
    let index = v:beval_col
    for l in getline(1, v:beval_lnum - 1)
        let index += len(l) + 1
    endfor
    return index
endfunction

function s:getCurrentLineOffset()
    let index = 0
    for l in getline(1, line('.') - 1)
        let index += len(l) + 1
    endfor
    return index
endfunction

function s:getCursorOffset()
    return <SID>getCurrentLineOffset() + col('.')
endfunction

function s:getFileName()
    return expand("%:p")
endfunction

function s:getCompletionStart()
    wa!
    let line = getline('.')
    let i = col('.') - 1
    while i > 0
        let value = line[i - 1]
        if value == '.' || value == ' '
            return i
        endif
        let i -= 1
    endwhile
    return i
endfunction

function CodeFellowComplete(findstart, base)
    " TODO Detect which completion type to use
    return CodeFellowCompleteMember(a:findstart, a:base)
endfunction

function CodeFellowCompleteMember(findstart, base)
    if a:findstart
        return <SID>getCompletionStart()
    else
        " Reset omnifunc to default
        call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowComplete")

        echo "CodeFellow: Please wait..."
        " Get position in current line
        let typePos = 0
        let i = col('.') - 1
        while i > 0
            let value = getline('.')[i]
            if value != ' '
                let typePos = i - 1
                break
            endif
            let i -= 1
        endwhile

        " Add all lines above
        let typePos += <SID>getCurrentLineOffset()

        let result = <SID>SendMessage("CompleteMember", expand("%:p"), typePos, a:base)

        let res = []
        for entryLine in split(result, "\n")
            let entry = split(entryLine, ";")
            call add(res, {'word': entry[0], 'abbr': entry[0] . entry[1], 'icase': 0})
        endfor

        return res
    endif
endfunction

function CodeFellowBalloonType()
    let bufmod = getbufvar(bufnr(bufname("%")), "&mod")
    if bufmod == 1
        return "Save buffer to get type information"
    else
        let result = <SID>SendMessage("TypeInfo", <SID>getFileName(), <SID>getMousePointerOffset())
        return result
    endif
endfunction

function CodeFellowPrintTypeInfo()
    let bufmod = getbufvar(bufnr(bufname("%")), "&mod")
    if bufmod == 1
        echo "Save buffer to get type information"
    else
        echo <SID>SendMessage("TypeInfo", <SID>getFileName(), <SID>getCursorOffset())
    endif
endfunction

function CodeFellowReloadFile()
    return <SID>SendMessage("ReloadFile", <SID>getFileName())
endfunction


