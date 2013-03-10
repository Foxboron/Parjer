# parjer

A very basic IRCBot in Clojure.

## Usage

```clojure
{:nick "Cjoey"
 :servers [{:server "irc.blabla.com"
            :port 6667
            :chans #{"#lobby"}}
           {:server "irc.blabla.com"
            :port 13337
            :chans #{"#bot"}}]
 :owner #{"Foxboron", "Guy2"}
 :mark "@"}
```
`lein deps`
`lein run`

The setup file should be simple enough.  

The bot itself got some basic commands, use help and try guess :D  

## License

Copyright © 2013 Morten "Foxboron" Linderud

Distributed under the Eclipse Public License, the same as Clojure.
