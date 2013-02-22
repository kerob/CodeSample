import curses, socket, sys, traceback, curses.textpad, select, random, os

class Globals:
  screen = None
  clientNames = []
  clients = []
  items = []

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

class Client:
  def __init__(self, sock, name):
    self.name = name
    self.sock = sock
    self.bill = 0
    self.wonItems = []

  def checkSocket(self, sock):
    "Determine whether a socket belongs to this client."
    if self.sock == sock:
      return True
    return False

  def close(self):
    self.sock.close()
    self.sock = None

class Item:
  itemNumCount = 0

  def __init__(self, curBid = 0):
    Item.itemNumCount += 1
    self.itemNum = Item.itemNumCount
    self.curBid = curBid

  def bid(self, itemNum, value):
    "Return true or false based on whether the bid was accepted."
    if itemNum != self.itemNum or value <= self.curBid:
      return False

    self.curBid = value
    return True

def main():
  #Initialize all the curses-related stuff
  initCurses()

  #Initilize the two main windows - the bottom one has 7 lines, and the top
  #one will use whatever screen space is left over from that.
  screenMaxYX = Globals.screen.getmaxyx()
  divPoint = screenMaxYX[0] - 7
  upperWin = Window(divPoint, screenMaxYX[1], 0, 0, 'Main')
  lowerWin = TextWindow(7, screenMaxYX[1], divPoint, 0, 'LowerWin')

  #Initialize the networking part
  getClients(upperWin, lowerWin)

  #Print out the instructions
  upperWin.writeLine('Use -n to create a new item. Use -c to close the auction.')
  lowerWin.resetCursor()

  #Call the main loop function
  mainLoop(upperWin, lowerWin)

  #End the auction
  endAuction()

  #Reclaim the terminal from curses
  restoreScreen()

def getClients(upperWin, lowerWin):
  "Wait for all the clients from the bidder file to connect."
  upperWin.writeLine('Waiting for clients...')
  lowerWin.resetCursor()

  #Open the bidder file
  fh = open(sys.argv[1])
  clientNames = fh.readlines()

  numClients = len(clientNames)

  #Accept the conections
  listener = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
  listener.bind(('', eval(sys.argv[2])))
  listener.listen(5)

  #Keep accepting connections and decrementing numClients until numClients is 0
  while numClients > 0:
    (client, addr) = listener.accept()
    flo = client.makefile('rw', 0)
    client.close()

    #Determine the user's name
    username = flo.readline()
    if not username in clientNames:
      #Reject the connection and don't let i increment, since the conection wasn't valid
      flo.close()
      continue

    clientNames.remove(username)
    username = username.strip('\n')
    numClients -= 1
    Globals.clients.append(Client(flo, username))

  listener.close()

  upperWin.writeLine('All clients have connected. The auction has begun.')
  upperWin.writeLine('')
  lowerWin.resetCursor()

def mainLoop(upperWin, lowerWin):
  "The main application loop."
  currentItem = None
  highestBidder = None
  cont = True
  while cont:
    #Initiate the rlist, consisting of stdin and all the clients
    rlist = [sys.stdin]
    for client in Globals.clients:
      if not client.sock:
        continue
      rlist.append(client.sock)

    #Determine which inputs are ready, and shuffle them for fairness
    ready = select.select(rlist, [], [])
    ready = list(ready)
    random.shuffle(ready)

    for input in ready[0]:
      if input == sys.stdin:
        c = sys.stdin.read(1)

        #Check for a return key press
        if c == '\r':
          inStr = lowerWin.flush()
          if inStr == '\q': #Quit command
            return
          elif inStr == '-n': #New item command
            if currentItem != None:
              declareWinner(upperWin, lowerWin, highestBidder, currentItem)

            currentItem = Item()
            highestBidder = None
            broStr = 'Bidding for item #' + str(currentItem.itemNum) + ' has begun. Starting bid is ' + \
              str(currentItem.curBid) + '.'
            broadcast(upperWin, lowerWin, broStr)
          elif inStr == '-c': #Close auction command
            if currentItem != None:
              declareWinner(upperWin, lowerWin, highestBidder, currentItem)

            #Ending messages are handled client-side now.
            #broadcast(upperWin, lowerWin, 'The auction is now closed.')
            return

          else:
            #Send out message to everyone
            broadcast(upperWin, lowerWin, inStr)

        else:
          lowerWin.writeChar(c)

      else: #Input is coming from one of the clients
        bidStr = input.readline()

        #Check to see if the client has acutally disconnected.
        if not bidStr:
          for client in Globals.clients:
            if client.checkSocket(input):
              client.close()
              broadcast(upperWin, lowerWin, client.name + ' has disconnected.')
          continue

        bid = parseInput(bidStr)

        if currentItem != None:
          if currentItem.bid(bid[0], bid[1]):
            #Find out which client this socket belongs to
            for client in Globals.clients:
              if client.checkSocket(input):
                broStr = client.name + ' bids ' + str(bid[1]) + '.'
                broadcast(upperWin, lowerWin, broStr)
                highestBidder = client
          else: #The bid was invalid
            sendMessage(input, 'Your bid was rejected.')

def declareWinner(upperWin, lowerWin, winner, item):
  itemNum = str(item.itemNum)
  if winner == None:
    broadcast(upperWin, lowerWin, 'No bidders for item #' + itemNum + '.')
    return

  broadcast(upperWin, lowerWin, winner.name + ' has won item #' + itemNum)
  winner.wonItems.append(item)

def endAuction():
  "Close all connections and send out the emails."
  for client in Globals.clients:
    if client.sock != None:
      client.sock.close()

    #Make sure they actually bought something.
    if client.wonItems == []:
      continue

    totalCost = 0
    email = 'Congratulations on winning the following items:\n\n'
    for item in client.wonItems:
      email = email + '#' + str(item.itemNum) + ' - $' + str(item.curBid) + '\n'
      totalCost += item.curBid
    email = email + '\nYour grand total is: $' + str(totalCost)

    sendMail(client.name, email)

def sendMail(toAddr, msg):
  sendmail_location = '/usr/sbin/sendmail' # sendmail location
  emailPort = os.popen('%s -t' % sendmail_location, 'w')

  emailPort.write('From: auction@superawesomeauctions.com\n')
  emailPort.write('To: %s\n' % toAddr)
  emailPort.write('Subject: Auction Bill\n')
  emailPort.write('\n') #blank line seperating the body
  emailPort.write(msg)
  emailPort.close()

def debugEmail():
  "A debug function that prints the emails instead of actually sending them."
  for client in Globals.clients:
    for item in client.wonItems:
      print client.name + ' won item #' + str(item.itemNum) + ' for ' + str(item.curBid) + '.'

def broadcast(upperWin, lowerWin, broStr):
  "Send message to all clients"
  upperWin.writeLine(broStr)
  lowerWin.resetCursor()

  for client in Globals.clients:
    #Make sure the port is still valid
    if not client.sock:
      continue
    client.sock.writelines(broStr + '\n')

def sendMessage(sock, msg):
  sock.writelines(msg + '\n')

def parseInput(parseStr):
  "Parse the input from clients (error checking was handled on the client side"
  parseStr = parseStr.split()

  itemNum = int(parseStr[0])
  bid = int(parseStr[1])

  return (itemNum, bid)

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
