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

 Uses clojail to sandbox the input. You need a `~/.java.policy` file to use this feature.

* dice

  Returns 4. I am 100% sure this is a random number.

* say

  Return what ever typed into it.

* join

  Joins a channel.

* part
  Parts a channel


## License

Copyright © 2013 Morten "Foxboron" Linderud

Distributed under the Eclipse Public License, the same as Clojure.
