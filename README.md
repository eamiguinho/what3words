
# What3Words Tech challenge

The propose of this app is to use what3words API wrapper.

 - [x] Take in a three word address and return the location
 - [x] Add focus parameters using the phone location
 - [x] Use autosuggest to return results at the end
 - [x] Use autosuggest to return a list of partial results (index.home.ra___) → index.home,raft, index.home.rafts and display live in the list or via typeahead
 - [x] Look at our voice API and discuss in interview how you’d stream audio/return results - *integrated using OkHttp WebSocket*
   
Candidate: Emanuel Amiguinho

## Notes

Used *MVVM* architecture, *Dagger2*, *Okhttp for WebSocket*, *Coroutines*, *LiveData*, *what3words wrapper*.

## Demo
[Click to watch demo](https://drive.google.com/file/d/18gqkqqNlf6iQQHNDd8Xrv6korJCuLIfH/view?usp=sharing)
