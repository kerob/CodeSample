import curses, socket, sys, traceback, curses.textpad, select

class Globals:
  screen = None
  srvrSocket = None

class Window:
  def __init__(self, lines, cols, y, x, windowName):
    #Define the outer window for the borders
    self.outerWin = Globals.screen.subwin(lines, cols, y, x)

    #Define the inner window (the writeable space)
    self.innerWin = self.outerWin.subwin(lines - 2, cols - 2, \
      1 + y, 1 + x)

    #Determine initial coordinates
    self.x = 0
    self.y = 0

    #Determine inner window dimensions
    maxYX = self.innerWin.getmaxyx()
    self.lines = maxYX[0]
    self.cols = maxYX[1]

    self.outerWin.border()
    self.outerWin.addstr(0, 1, windowName)
    self.outerWin.refresh()

  def writeLine(self, line):
    #Determine how long the "line" is.
    lines = []
    while len(line) > self.cols:
      lines.append(line[:self.cols])
      line = line[self.cols:]

    #Append the remainder
    lines.append(line[:len(line)])

    #The lines need to be printed in reverse
    lines.reverse()

    #Insert a sufficient number of lines
    for nextLine in lines:
      self.innerWin.move(0, 0)
      self.innerWin.insertln()
      self.innerWin.addstr(0, 0, nextLine)

    self.innerWin.refresh()

class TextWindow(Window):
  def __init__(self, lines, cols, y, x, windowName):
    Window.__init__(self, lines, cols, y, x, windowName)
    self.innerWin.move(0, 0)
    self.innerWin.refresh()
    self.buffer = ''

  def writeChar(self, c):
    "Writes the specified char to the screen, assuming there is room."
    #Check to see if this is a special character (like a backspace)
    if c == '\x7f':
      self.backspace()
      return

    #Check that there is actually some room (discard any excess characters)
    if self.y == self.lines - 1 and self.x == self.cols - 1:
      return

    self.innerWin.addch(self.y, self.x, c)
    self.buffer = self.buffer + c

    #Determine the new coordinates
    self.x += 1
    if self.x == self.cols:
      #Incrementing x would drive you into the borders, so go to new line
      self.y += 1
      self.x = 0

    #Update the coordinates
    self.innerWin.move(self.y, self.x)
    self.innerWin.refresh()

  def backspace(self):
    #Make sure you're not at the first character
    if self.y == 0 and self.x == 0:
      return

    #Check to see if we're at the end of a line
    if self.x == 0:
      self.y -= 1
      self.x = self.cols - 1
    else:
      self.x -= 1

    self.innerWin.addch(self.y, self.x, ' ')
    self.buffer = self.buffer[:-1]

    #Update the cursor position
    self.innerWin.move(self.y, self.x)
    self.innerWin.refresh()

  def flush(self):
    "Clear the screen and buffer, and return the buffer text."
    temp = self.buffer
    self.buffer = ''

    self.innerWin.erase()
    self.innerWin.refresh()
    self.y = 0
    self.x = 0
    self.innerWin.move(self.y, self.x)
    self.innerWin.refresh()   

    return temp

  def resetCursor(self):
    self.innerWin.move(self.y, self.x)
    self.innerWin.refresh()

def main():
  #Prompt for username
  username = raw_input('Please enter your e-mail address: ')

  #Initialize all the curses-related stuff
  initCurses()

  #Initilize the two main windows - the bottom one has 7 lines, and the top
  #one will use whatever screen space is left over from that.
  screenMaxYX = Globals.screen.getmaxyx()
  divPoint = screenMaxYX[0] - 7
  upperWin = Window(divPoint, screenMaxYX[1], 0, 0, 'Main')
  lowerWin = TextWindow(7, screenMaxYX[1], divPoint, 0, 'LowerWin')

  #Connect to the server
  connect(username)

  #Call the main loop function
  mainLoop(upperWin, lowerWin)

  #End the connection
  Globals.srvrSocket.close()

  #End the auction
  upperWin.writeLine('')
  upperWin.writeLine('Press any key to continue.')
  upperWin.writeLine('The auction has ended. A bill will be sent to your email address.')
  lowerWin.resetCursor()
  lowerWin.innerWin.getch()

  #Reclaim the terminal from curses
  restoreScreen()

def connect(username):
  sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  host = sys.argv[1]
  port = int(sys.argv[2])
  sock.connect((host, port))

  flo = sock.makefile('rw', 0)

  #Send client name to server
  flo.writelines(username + '\n')

  Globals.srvrSocket = flo

def mainLoop(upperWin, lowerWin):
  "The main application loop."
  cont = True
  while cont:
    rlist = [sys.stdin, Globals.srvrSocket]
    ready = select.select(rlist, [], [])

    for input in ready[0]:
      if input == sys.stdin:
        c = sys.stdin.read(1)

        #Check for a return key press
        if c == '\r':
          inStr = lowerWin.flush()
          if inStr == '\q':
            return

          #Check that this is valid input and send it to the server if it is
          if not checkInput(inStr):
            upperWin.writeLine('Invalid input. Format is [item number] [bid].')
            lowerWin.resetCursor()
            continue

          Globals.srvrSocket.writelines(inStr + '\n')
        else:
          lowerWin.writeChar(c)

      else: #Input is from the server
        srvrStr = input.readline()

        if not srvrStr:
          return

        srvrStr = srvrStr.strip('\n')
        upperWin.writeLine(srvrStr)
        lowerWin.resetCursor()

def checkInput(input):
  "Make sure the input is valid before sending it to the server."
  input = input.split()

  if len(input) != 2:
    return False

  #Make sure they're both integers
  try:
    int(input[0])
    int(input[1])
  except:
    return False

  return True

def initCurses():
  Globals.screen = curses.initscr()
  curses.noecho()
  curses.cbreak()

def restoreScreen():
  curses.nocbreak()
  curses.echo()
  curses.endwin()

if __name__ == '__main__':
  try:
    main()
  except:
    print 'An error has occurred.'
    if Globals.screen != None:
      restoreScreen()
    traceback.print_exc()
