# mnPrintClient
**Java PDF Print Client**

Silently print PDF files from your server by reading a JSON parameter list, or from your local
computer by placing PDF files in the specified directory.

Usage: mnprintclient -[dir|url] path|url [options]

## Options

| Option       | Description                                                  |
| ------------ | ------------------------------------------------------------ |
| -url         | Url to poll with a valid JSON response                       |
| -dir         | Directory to poll where pdf files are placed to print        |
| -processed   | Directory to move printed pdf files in dir mode              |
| -cookie      | String with cookie to send to server in url mode             |
| -printerName | Default printer name (useful in directory mode)              |
| -copies      | Default number of copies to print (useful in directory mode) |
| --help       | Prints this page                                             |

## Examples

`mnprintclient -dir c:\users\user\documents\toprint -printerName MyLaserPrinter`
`mnprintclient -url http://www.server.com/printList.json -cookie JSESSIONID=12a3bc4defgjk5lmn6opqr7stu`
