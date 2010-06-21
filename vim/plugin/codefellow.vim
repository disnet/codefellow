" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1


if exists("loaded_codefellow")
    finish
endif
let loaded_codefellow=1

let s:vimhomepath = split(&runtimepath, ',')
let s:codefellowpath =  s:vimhomepath[0] . "/codefellow/"


autocmd FileType scala set omnifunc=CodeFellowComplete
autocmd FileType scala set ballooneval
autocmd FileType scala set balloondelay=300
autocmd FileType scala set balloonexpr=CodeFellowBalloonType()

" TODO: Need to do this in the background
"autocmd BufWritePost *.scala call <SID>ReloadFile(expand("%:p"))

" Backup
"autocmd FileType scala imap <buffer> <C-s><C-m> <C-\><C-O>:call <CR>


autocmd FileType scala imap <buffer> <C-s><C-m> <C-\><C-O>:py hello()<CR>
python << endpython

def hello():
    print "HELLO"
    print __name__

endpython

function s:RunClient(...)
    let params = ""
    let argnum = 1
    while argnum <= a:0
        let params = params . '"' . a:{argnum} . '" '
        let argnum += 1
    endwhile
    let cmd = s:codefellowpath . 'client.sh "' . expand("%:p") . '" ' . params
    return system(cmd)
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

        let result = <SID>RunClient("CompleteMember", expand("%:p"), typePos, a:base)

        let res = []
        for entryLine in split(result, "\n")
            let entry = split(entryLine, "|")
            call add(res, {'word': entry[0], 'abbr': entry[0] . entry[1], 'icase': 0})
        endfor

        return res
    endif
endfunction

"function s:ReloadFile(file)
    "call <SID>RunClient("ReloadFile", a:file)
"endfunction


function CodeFellowBalloonType()
    let index = v:beval_col
    for l in getline(1, v:beval_lnum - 1)
        let index += len(l)
    endfor

    let result = <SID>RunClient("TypeInfo", expand("%:p"), index)
    return result
endfunction





