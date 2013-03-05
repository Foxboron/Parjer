# parjer

A very basic IRCBot in Clojure.

## Usage

```clojure
{:nick "Cjoey"
 :server "irc.codetalk.io"
 :port 6667
 :chan "#lobby"
 :owner #{Foxboron}
 :mark "@"}
```
The setup file should be simple enough.

The bot itself got some basic commands:
* eval
* dice
* say
* join
* part

## License

Copyright © 2013 Morten "Foxboron" Linderud

Distributed under the Eclipse Public License, the same as Clojure.
