" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1

if exists("loaded_codefellow")
    finish
endif
let loaded_codefellow=1

" OmniCompletion
autocmd FileType scala setlocal omnifunc=CodeFellowComplete

" Balloon type information
autocmd FileType scala setlocal ballooneval
autocmd FileType scala setlocal balloondelay=300
autocmd FileType scala setlocal balloonexpr=CodeFellowBalloonType()

" Need to do this in the background
"autocmd BufWritePost *.scala call <SID>ReloadFile(expand("%:p"))

function s:SendMessage(type, ...)
python << endpython
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

s.sendall(msg)
s.sendall("\nENDREQUEST\n")

data = ""
while 1:
    tmp = s.recv(1024)
    if not tmp:
        break
    data += tmp
vim.command('return "' + data + '"')
endpython
endfunction

function s:getCursorIndex()
    let index = v:beval_col
    for l in getline(1, v:beval_lnum - 1)
        let index += len(l)
    endfor
    return index
endfunction

function CodeFellowComplete(findstart, base)
    let line = getline('.')
    if a:findstart
        wa!
        let i = col('.') - 1
        while i > 0
            let value = line[i - 1]
            if value == '.' || value == ' '
                return i
            endif
            let i -= 1
        endwhile
        return i
    else
        " Get position in current line
        let typePos = 0
        let i = col('.') - 1
        while i > 0
            let value = line[i]
            if value != ' '
                let typePos = i - 1
                break
            endif
            let i -= 1
        endwhile

        " Add all lines above
        for l in getline(1, line('.') - 1)
            let typePos += len(l)
        endfor

        let result = <SID>SendMessage("CompleteMember", expand("%:p"), typePos, a:base)

        let res = []
        for entryLine in split(result, "\n")
            let entry = split(entryLine, "|")
            call add(res, {'word': entry[0], 'abbr': entry[0] . entry[1], 'icase': 0})
        endfor

        return res
    endif
endfunction


function CodeFellowBalloonType()
    let index = <SID>getCursorIndex()
    let result = <SID>SendMessage("TypeInfo", expand("%:p"), index)
    return result
endfunction





