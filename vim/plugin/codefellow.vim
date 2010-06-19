" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1


function codefellow#CompleteScope(findstart, base)
    if a:findstart
        let line = getline('.')
        let i = col('.') - 1
        while i > 0
            let l:value = line[i - 1]
            if l:value == '.'
                return i
            "else if l:value == ' '
                "while l:value == ' '
                    "let i -= 1
                    "let l:value = line[i - 1]
                "endwhile
                "return i
            endif
            let i -= 1
        endwhile
        return i
    else
        let l:pos = 0
        for l in getline(1, line('.') - 1)
            let l:pos += len(l)
        endfor
        let l:pos += col('.')
        let l:pos -= 2 
        
        let l:cmd = '~/.vim/codefellow/client.sh "' . expand("%:p") . '" CompleteScope "' . expand("%:p") . '" ' . l:pos . ' ' . "h"
        call input(l:cmd)
        let l:result = system(l:cmd)
        call input(l:result)


        let res = []
        call add(res, {'word': 'Theword1', 'abbr': a:base . 'word1', 'icase': 1})
        call add(res, {'word': 'Theword2', 'abbr': 'word2', 'icase': 1})
        call add(res, {'word': 'Theword3', 'abbr': 'word3', 'icase': 1})
        return res
    endif
endfunction




