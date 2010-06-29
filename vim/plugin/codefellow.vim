" Author: Roman Roelofsen <roman.roelofsen@gmail.com>
" Version: 0.1

if exists("loaded_codefellow")
    finish
endif
let loaded_codefellow=1


" filetype
augroup scala
  au BufRead,BufNewFile *.scala set filetype=scala
augroup end

" OmniCompletion
autocmd FileType scala setlocal omnifunc=CodeFellowComplete

if !exists('g:codefellow_no_default_mappings')
  " default completion: (<c-s> will not work in console Vim !):
  " note the noremap which will not cause an infinite loop
  autocmd FileType scala inoremap <buffer> <C-x><C-o> <C-O>:set omnifunc=codefellow#Complete<CR><c-x><c-o>

  " special mappings calling special completion functions
  autocmd FileType scala inoremap <buffer> <C-s><C-m> <C-O>:set omnifunc=codefellow#CompleteMember<CR><c-x><c-o>
  autocmd FileType scala inoremap <buffer> <C-s><C-s> <C-O>:set omnifunc=codefellow#CompleteScope<CR><c-x><c-o>
  autocmd FileType scala inoremap <buffer> <C-s><C-n> <C-O>:set omnifunc=codefellow#CompleteSmart<CR><c-x><c-o>
  autocmd FileType scala noremap <buffer> <C-s><C-t> <esc>:<c-u>call codefellow#PrintTypeInfo()<CR>
  autocmd FileType scala noremap <buffer> <F1> :call codefellow#PrintTypeInfo()<CR>
endif

" Balloon type information
if has("balloon_eval")
    autocmd FileType scala setlocal ballooneval
    autocmd FileType scala setlocal balloondelay=300
    autocmd FileType scala setlocal balloonexpr=codefellow#BalloonType()
endif

" Compilation on save
autocmd BufWritePost *.scala call codefellow#ReloadFile()
