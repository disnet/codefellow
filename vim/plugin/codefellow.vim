" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1

if exists("loaded_codefellow")
    finish
endif
let loaded_codefellow=1

" OmniCompletion
autocmd FileType scala setlocal omnifunc=CodeFellowComplete
autocmd FileType scala inoremap <buffer> <C-s><C-m> <C-O>:call CodeFellowTriggerCompleteMember()<CR>
autocmd FileType scala inoremap <buffer> <C-s><C-s> <C-O>:call CodeFellowTriggerCompleteScope()<CR>
"autocmd FileType scala inoremap <buffer> <C-s><C-n> <C-O>:call CodeFellowTriggerCompleteSmart()<CR>
autocmd FileType scala noremap <buffer> <C-s><C-t> :call CodeFellowPrintTypeInfo()<CR>

" Balloon type information
if has("balloon_eval")
    autocmd FileType scala setlocal ballooneval
    autocmd FileType scala setlocal balloondelay=300
    autocmd FileType scala setlocal balloonexpr=CodeFellowBalloonType()
endif

" Compilation on save
autocmd BufWritePost *.scala call CodeFellowReloadFile()

"
" Sends a message to the CodeFellow server and return the response
"
function s:SendMessage(type, ...)
python << endpython
import socket
import vim
try:
    s = socket.socket()
    s.connect(("localhost", 9081))

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

"
" Returns the absolute path of the current file
"
function s:getFileName()
    return expand("%:p")
endfunction

"
" Returns the index in the current line where the word under the cursor starts
"
function s:getWordUnderCursorIndex()
    let line = getline('.')
    let i = col('.')
    while i > 0
        let value = line[i - 1]
        if value == '.' || value == ' ' " TODO: Improve check, e.g. for '(' ')' ';'
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

function CodeFellowTriggerCompleteMember()
    call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowCompleteMember")
    call feedkeys("\<C-X>\<C-O>", "n")
endfunction

function CodeFellowCompleteMember(findstart, base)
    if a:findstart
        return <SID>getWordUnderCursorIndex()
    else
        w!
        echo "CodeFellow: Please wait..."
        " Reset omnifunc to default
        call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowComplete")

        let result = <SID>SendMessage("CompleteMember", <SID>getFileName(), line(".") - 1, col("."), a:base)
        let res = []
        for entryLine in split(result, "\n")
            let entry = split(entryLine, ";")
            call add(res, {'word': entry[0], 'abbr': entry[0] . entry[1], 'icase': 0})
        endfor
        return res
    endif
endfunction

function CodeFellowTriggerCompleteScope()
    call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowCompleteScope")
    call feedkeys("\<C-X>\<C-O>", "n")
endfunction

function CodeFellowCompleteScope(findstart, base)
    if a:findstart
        return <SID>getWordUnderCursorIndex()
    else
        w!
        echo "CodeFellow: Please wait..."
        " Reset omnifunc to default
        call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowComplete")

        let result = <SID>SendMessage("CompleteScope", <SID>getFileName(), line(".") - 1, col("."), a:base)
        let res = []
        for entryLine in split(result, "\n")
            let entry = split(entryLine, ";")
            call add(res, {'word': entry[0], 'abbr': entry[0] . " (" . entry[1] . ")", 'icase': 0})
        endfor
        return res
    endif
endfunction

function CodeFellowTriggerCompleteSmart()
    call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowCompleteSmart")
    call feedkeys("\<C-X>\<C-O>", "n")
endfunction

"function CodeFellowCompleteSmart(findstart, base)
    "if a:findstart
        "return <SID>getWordUnderCursorIndex()
    "else
        "w!
        "echo "CodeFellow: Please wait..."
        "" Reset omnifunc to default
        "call setbufvar(bufnr(bufname("%")), "&omnifunc", "CodeFellowComplete")
"
        "let offset = <SID>getWordBeforeCursorOffset()
        "let result = <SID>SendMessage("CompleteSmart", expand("%:p"), offset, a:base)
"
        "let res = []
        "for entryLine in split(result, "\n")
            "let entry = split(entryLine, ";")
            "call add(res, {'word': entry[0], 'abbr': entry[0] . " (" . entry[1] . ")", 'icase': 0})
        "endfor
        "return res
    "endif
"endfunction

function CodeFellowBalloonType()
    let bufmod = getbufvar(bufnr(bufname("%")), "&mod")
    if bufmod == 1
        return "Save buffer to get type information"
    else
        let result = <SID>SendMessage("TypeInfo", <SID>getFileName(), v:beval_lnum - 1, v:beval_col)
        return result
    endif
endfunction

function CodeFellowPrintTypeInfo()
    let bufmod = getbufvar(bufnr(bufname("%")), "&mod")
    if bufmod == 1
        echo "Save buffer to get type information"
    else
        echo <SID>SendMessage("TypeInfo", <SID>getFileName(), line(".") - 1, col("."))
    endif
endfunction

function CodeFellowReloadFile()
    return <SID>SendMessage("ReloadFile", <SID>getFileName())
endfunction


