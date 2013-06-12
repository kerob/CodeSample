#Breakout / Alleyway clone
#Kyle Robles

from livewires import color
import games

games.init(screen_width = 640, screen_height = 480, fps = 50)

class Player(games.Sprite):
	"""Main object user interacts with"""
	#Set Image and ball conditional
	image = games.load_image("bouncer.jpg")
	ballOut = False
	
	#Initialize screen position of player
	def __init__(self):
		super(Player, self).__init__(image = Player.image,
									x = games.screen.width/2,
									bottom = games.screen.height - 30)
										
	#Check movement and set boundaries
	def update(self):
		#Keyboard controls with a and s
		if games.keyboard.is_pressed(games.K_a):
			self.x -= 5
		if games.keyboard.is_pressed(games.K_s):
			self.x += 5
		if games.keyboard.is_pressed(games.K_SPACE) and self.ballOut == False:
			ball = Ball()
			games.screen.add(ball)
			self.ballOut = True		
		#Insure that the player sprite stays with the range of vision
		if self.left < games.screen.width/4 + 20:
			self.left = games.screen.width/4 + 20
		if self.right > (games.screen.width - games.screen.width/4 - 20):
			self.right = games.screen.width - games.screen.width/4 - 20
			
	#For ball interaction purposes
	def checkCollide(self):
		return 1
	
class Ball(games.Sprite):
	"""Ball object"""
	#set image, sound and initial speed
	image = games.load_image("ball.jpg")
	speed = 1
	sound = games.load_sound("Blop.wav")
	
	#initialize ball and score
	def __init__(self):
		super(Ball, self).__init__(image = Ball.image,
									x = games.screen.width/2,
									y = games.screen.height/2,
									dx = 1,
									dy = 1)
		self.score = games.Text(value = 0, size = 40, color = color.white, top = 10, right = games.screen.width - 50)
		games.screen.add(self.score)
	
	#Function to check which direction to send ball after bounce
	def ricochetDir(self, bounce, valCheck):
			#Conditionals here make sure speed increases without changing the ball's current direction
			if valCheck == 1:
				if self.dx < 0:
					self.dx = - (abs(self.dx) + self.speed*.02)
				if self.dy < 0:
					self.dy = - (abs(self.dy) + self.speed*.02)
				if self.dx > 0:
					self.dx += self.speed*.02
				if self.dy > 0:
					self.dy += self.speed*.02
			#Change direction depending on area of contact
			if (self.right > bounce.left or self.left < bounce.right) and ((abs(self.right - bounce.left) or abs(self.left - bounce.right)) < (abs(self.bottom - bounce.top) or abs(self.top - bounce.bottom))):
				self.dx = -self.dx
			if (self.bottom > bounce.top or self.top < bounce.bottom) and ((abs(self.right - bounce.left) or abs(self.left - bounce.right)) > (abs(self.bottom - bounce.top) or abs(self.top - bounce.bottom))):
				self.dy = -self.dy
	
	def update(self):
		#Handle collisions on ball object
		#Check if ball hits the floor
		if self.bottom > games.screen.height:
			self.destroy()
			Player.ballOut = False
			self.gameOver("Game Over")
		#Next two conditionals check to keep ball in field of play
		if self.top < 0: #Check for bounce off top screen
			self.dy = -self.dy
			self.sound.play()
		#Check for collisions on walls, blocks and player
		for bounce in self.overlapping_sprites:
			self.sound.play()
			temp = bounce.checkCollide()
			if temp == 1:#Check for bounce off player
				#change vector
				self.ricochetDir(bounce, 0)
			if temp == 2: #Signals a block is hit
				#destroy block
				bounce.handleCollide()
				#change vector
				self.ricochetDir(bounce, 1)
				#increase speed
				self.speed += .01
				#increase score
				self.score.value += 10
				#check if all blocks are destroyed and end game
				if self.score.value == 800:
					self.gameOver("You Win")
					self.dx = 0
					self.dy = 0
			if temp == 3: #Check if bouncing off border, only change horizontal direction
				self.dx = -self.dx
			self.sound.play()
				
	def gameOver(self, msg):
		endMsg = games.Message(value = msg, size = 75, color = color.red, x = games.screen.width/2,
								y = games.screen.height/2, lifetime = 5*games.screen.fps, after_death = games.screen.quit)
		games.screen.add(endMsg)
	
class Block(games.Sprite):
	"""Block obstacle object"""
	def checkCollide(self):
		return 2
		
	def handleCollide(self):
		self.destroy()
	
class sideBorder(games.Sprite):
	"""Border obstacle object"""
	def checkCollide(self):
		return 3
	
	
#class topBorder(games.sprite):
#	"""Descending Border object"""

def buildBlocks():
	#Set the various block colors
	redB = games.load_image("topblock.jpg")
	blueB = games.load_image("midblock.jpg")
	greenB = games.load_image("lowblock.jpg")
	yelB = games.load_image("botblock.jpg")
	
	#set initial position
	topCor = 50
	
	#Build a 8x10 grid of blocks for the level
	for down in range(0,8):
		rowCount = 230
		if down < 1:
			imgBuffer = redB
		if down < 3 and down > 1:
			imgBuffer = blueB
		if down < 5 and down > 3:
			imgBuffer = greenB
		if down < 8 and down > 5:
			imgBuffer = yelB
		for across in range(0,10):
			placeBlk = Block(image = imgBuffer,
							x = rowCount,
							y = topCor)
			games.screen.add(placeBlk)
			rowCount += 20
		topCor += 10
	
def main():
	"""Start the main game"""
	#Initialize player location, block placements and ball placement
	user = Player()
	games.screen.add(user)
	
	borderimage = games.load_image("sideborder.jpg")
	border1 = sideBorder(image = borderimage, 
						x = games.screen.width/4, 
						y = games.screen.height/2)
	border2 = sideBorder(image = borderimage,
						x = games.screen.width - games.screen.width/4, 
						y = games.screen.height/2)
	games.screen.add(border1)
	games.screen.add(border2)
	
	infoTxt = games.Text(value = "Controls", size = 30, color = color.white, top = 10, left =  10)
	infoTxt2 = games.Text(value = "A: Left", size = 10, color = color.white, top = 60, left = 10)
	infoTxt3 = games.Text(value = "S: Right", size = 10, color = color.white, top = 90, left = 10)
	infoTxt4 = games.Text(value = "SPACE: Launch Ball", size = 10, color = color.white, top = 120, left = 10)
	
	games.screen.add(infoTxt)
	games.screen.add(infoTxt2)
	games.screen.add(infoTxt3)
	games.screen.add(infoTxt4)

	buildBlocks()
	#continue till death
	games.screen.mainloop()

#def titleScreen:
#	"""Display title screen with controls"""
	#Load up title screen with control information
	#if user import then
		#main()
		
main()