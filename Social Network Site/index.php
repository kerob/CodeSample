<?php
//
// session_start() creates a session or resumes the current one (based 
// on a session identifier passed via a cookie).  See
//
//     http://php.net/manual/en/features.sessions.php
//
// for details.
//
session_start();
?>
<html>
<head><basefont face="Arial"></head>
<title>Aggbook</title>
<body>

<?php
//
// Write your code here to open a connection to the database server
$dbconn = pg_connect("dbname=aggbook") or die('cannot connect to database:'. pg_last_error());
$index= pg_query($dbconn,"SELECT * from USERS");
$email= $_POST['email'];
$uname= $_POST['name'];
$pword= md5($_POST['password']);
$about= $_POST['about'];
$num= pg_num_rows($index) + 1;
if ($_POST['create']) {
//
// Write your code here to process a register new user request.
// This variable will be set if the user clicks "submit" on the 
// "Create Account" button.  See
//
// http://php.net/manual/en/reserved.variables.post.php
//
// for a description of the global $_POST variable.
//
$entry = pg_query_params($dbconn, 'INSERT INTO USERS(uID,uName,email,pWord,about,fcount) values($1,$2,$3,$4,$5,0)',array($num,$uname,$email,$pword,$about));


  if(!$entry){
  die('Cannot insert to databasei: '. pg_last_error());
 // echo "HOORAY";
  ?>
  <h2>Account creation failed!</h2>
  Click <a href="index.php">here</a> to return.
  <?php
  }
  else if($entry){

  echo "Welcome ".$uname."!";
  ?>
  <h2>Account created!</h2>
  Click <a href="index.php">here</a> to log in.
<?php
  }
} else if ($_POST['login']) {
  $_SESSION['active'] = $email;
 // echo $_SESSION['active'];
 // echo "Here:".$check;
//  echo "Test:";
  //echo $result;
//echo $num." rows returned";
  while($test= pg_fetch_object($index)){
  $comp = $test->email;
  $comp2= $test->pword;
//  echo $email;
//  echo $comp;
//  echo $comp2;
//  echo $prof; 
  if ((trim($comp) == trim($email))&& (trim($comp2) == trim($pword))){
//  echo "PROGRESS";
  $prof= $test->uid;
  $aname= $test->uname;
  $onabout= $test->about;
  $_SESSION['userID'] =  $prof;
  $_SESSION['name'] = $aname;
  $_SESSION['email'] = $email;
  $_SESSION['about'] = $onabout;
  // Write your code here to process a user-login request
  // If the user successfully logged in, then we return a "login success" message
  $success= true;
  break;} // hardcoded placeholder
  }
if ($success) {
?>
<h2>Login successful!</h2>
Click <a href="home.php?profile=<?php echo $_SESSION['userID'];
?>
">here</a> to continue.
<!-- 
By using "home.php?profile=1", we pass an argument, profile=1, via http GET 
to the home.php page.  The intended meaning is to indicate which profile should
be displayed.  (By default, it should be the user who just logged in.)
You might change the hardcoded "1" to, e.g., the id of the user from the underlying 
database.  See

http://www.php.net/manual/en/reserved.variables.get.php

for a description of the $_GET global variable.  Alternatively, information	
like this can also be passed around via the $_SESSION variable, see

http://www.php.net/manual/en/reserved.variables.session.php
 -->
<?php
    } else {
//echo $comp." ".$email." ".$comp2." ".$pword;
?>
<h2>Email address not found or password invalid</h2>
Click <a href="index.php">here</a> to try again.
<?php
    }
} else {

?>
<img src="youthmark.gif"/>
<h1>Welcome to Aggbook!</h1>
<table cellspacing="20">
  <tr valign="top"><td>
<h2>Create new account</h2>
<!-- 
The page will redirect to itself to process the form data when
 the user submits the "Create Account" button
-->
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="POST">
Name:<br/>
<input name="name" type="text" size="32"/>
<p/>
Email:<br/>
<input name="email" type="text" size="32"/>
<p/>
Password:<br/>
<input name="password" type="password" size="32"/>
<p/>
About me:<br/>
<textarea name="about" cols="32" rows="8">
</textarea>
<p/>
<input type="submit" name="create" value="Create Account"/>
</form>
</td>
<td>
<h2>Login to existing account</h2>
<!-- 
The page will redirect to itself to process the form data when
 the user clicks "Log in" 
-->
<form action="<?php echo $_SERVER['PHP_SELF']; ?>" method="POST">
Email:<br/>
<input name="email" type="text" size="32"/>
<p/>
Password:<br/>
<input name="password" type="password" size="32"/>
<p/>
<input type="submit" name="login" value="Log in"/>
</form>
  </td>
</tr>
</table>
<?php
}
?>

<?php
//
// Write your code here to close the connection to the database server
//
pg_close($dbconn);
?>
</body>
</html>
