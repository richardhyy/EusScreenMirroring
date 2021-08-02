# EusScreenMirroring
**Casting Your Screen to Minecraft Server**

## Features
- Customizable port(s) for receiving screencast
- User-definable screen size
- Show cursor position for slideshow purpose
- Password protection
- _(WIP) Ban IP if failed verifying for too many times_

## Screenshot
![Demo](image/demo_screenmirroring.gif)

## Commands

| Command                                                      | Description                                                  | Permission           |
| ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------- |
| /screenmirroring create [width: default=4] [height: default=3] | Create a new Screen Mirror                                   | screenmirroring.user |
| /screenmirroring get \<ID\> [PlayerName] [column(x)] [line(y)] | Get MapView for Mirror with specified ID                     | screenmirroring.user |
| /screenmirroring list                                        | List all Mirrors created by executor with connection passcode | screenmirroring.user |
| /screenmirroring connection                                  | Get the server’s connection information, i.e. UDP port       | screenmirroring.user |



## Permissions

`screenmirroring.user` : Default for OPs for basic screen mirroring commands

