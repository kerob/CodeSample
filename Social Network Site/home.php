<?php
// session_start() creates a session or resumes the current one based 
// on a session identifier passed via a GET or POST request, or 
// passed via a cookie. 
session_start();
// Write your code here to open a connection to the database server
$dbconn = pg_connect("dbname=aggbook") or die("Could not connect to database".pg_last_error());
$uid = $_SESSION['userID'];
$uidView= $_GET['profile'];
$useron= $_SESSION['name'];
$view = pg_query($dbconn, "SELECT distinct * from USERS where uid = '$uidView'");
//echo date("d/m/y h:i:s");
if($view){
  $page = pg_fetch_object($view);
  $pagename = $page->uname;
  $pagemail = $page->email;
  $pageabout = $page->about;
//  break;
  }
//}
?>
<html>
<head>
<!-- $_SESSION[] is an associative array containing session variables available to the current script. -->


<title>Welcome to Aggbook <?php echo $useron; ?></title>

</head>
<body>


<img src="youthmark.gif"/>
<h1>Welcome to Aggbook, <?php echo $useron; ?>!</h1>

<table cellspacing="20">
  <tr valign="top">
    <td>
      <h2>Viewing Profile</h2>
      <table>
	<tr><th>Name</th><td><?php echo $pagename; ?></td></tr>
	<tr><th>Email</th><td><i><?php echo $pagemail; ?></i></td></tr>
	<tr><th>About</th><td><?php echo $pageabout; ?></td></tr>
      </table>
    </td>

    <td>
      <h2>Friends</h2>
<?php
// deal with add/remove of friend
// $_POST[] is an associative array of variables passed to the current script via the HTTP POST method.
if ($_POST['add_friend'] || $_POST['remove_friend']) {
  // Write your code here to retrieve friend data from the database
  $find = $_POST['email'];
  $search = pg_query($dbconn,"select * from users where email = '$find'");
  $pals = pg_query($dbconn,"select * from relation where friend1 = '$uid'");
  $ispal = FALSE;
  $woo= pg_fetch_object($search);
  $reqf= $woo->uid;
  while($readin = pg_fetch_object($pals)){
    if(is_numeric($reqf)&&(trim($readin->friend2)==trim($reqf))){
    $ispal = TRUE;
    break;
  }
  }
//  $testvar = pg_fetch_object($search);
//  echo $testvar['uid'].$testvar->uid;
//echo $_POST['email'];  
if (!is_numeric($reqf)   /* Add some condition, we used a hardcoded value in the skeleton page */) {
      // Write your code here to handle the situation in which the email doesn't exist

?>
      <p/><b>Unrecognized email address</b><p/>
<?php
   } else if(trim($uid) == trim($reqf)){
?>
      <p/><b>Cannot Request Yourself as Friend</b><p/> 
<?php
    }else if($ispal){
?>
      <p/><b>User is already your friend</b><p/>
<?php    
    } else if ($_POST['add_friend']) {
      // Write your code here to handle the add_friend request
      pg_query_params($dbconn,'INSERT INTO PENDING(sender,recieve) VALUES($1,$2)',array($uid,$reqf));
?>
      <b>Friend request recorded</b><p/>
<?php
    } else {
      // Write your code here to handle the remove_friend request
     // $bye = $uid;
     // $search2 = pg_query($dbconn,'select * from relation(friend1,friend2)');
     // $bye2 = pg_fetch_row($search2);
     // echo $bye2;
     // echo $uid."Test:".$bye2['friend1']."Test2:".$bye2['friend2'];
      pg_query_params($dbconn,'DELETE FROM RELATION WHERE (friend1 = $2 and friend2 = $1) or (friend1 = $1 and friend2 = $2)',array($uid,$reqf)); 
      pg_query_params($dbconn,'UPDATE USERS set fcount = fcount - 1 where uid = $1 or uid = $2',array($uid,$reqf));
?>
      <b>Friend dropped</b><p/>
<?php
    }
} else if ($_POST['accept_request'] || $_POST['decline_request']) {
  // Write code here to process the accept_request/decline_request request
   $addf = $_POST['requestor']; 
      if ($_POST['accept_request']) {
	pg_query_params($dbconn,'INSERT INTO RELATION(friend1,friend2) VALUES($1,$2)',array($uid,$addf));
	pg_query_params($dbconn,'INSERT INTO RELATION(friend1,friend2) VALUES($1,$2)',array($addf,$uid));
	pg_query_params($dbconn,'UPDATE USERS set fcount = fcount + 1 where uid = $1 or uid = $2',array($uid,$addf));
        // Write code here to connect to database and update the database content in order to
        // reflect the acceptance of friend request

    }
echo $addf.$uid;
	pg_query_params($dbconn,'DELETE FROM PENDING where sender = $1 and recieve = $2',array($addf,$uid));
}

// Write your code here to retrieve friends, along with a count of their friends

?>
<table>
<?php
// Write your code here to fetch the friends data from the database
$flist = pg_query_params($dbconn,'select * from relation where friend1 = $1',array($uidView));
while ($friend = pg_fetch_object($flist) /* we used a hardcoded value here, but you should add your own while loop condtion */) {
$fpost = pg_query_params($dbconn,'select U.uName, U.email, U.fcount from users U where U.uid = $1',array($friend->friend2));
$fprint = pg_fetch_object($fpost);
?>
  <tr>
    <td><a href="?profile=<?php echo $friend->friend2; ?>"><?php echo $fprint->uname; ?></a></td>
    <td><i><?php echo $fprint->email; ?></i></td>
    <td>(<?php echo $fprint->fcount; ?> friend(s))</td>
  </tr>
<?php
}
?>
</table>
<?php
// Write your code here to fetch pending requests if a user is viewing
// his or her own page
if (trim($uid) == trim($uidView)) {
      $pendlist = pg_query_params($dbconn,'select * from pending where recieve= $1',array($uid));

//      $plist = pg_fetch_object($pendlist);
//	while($plist){
//      echo $plist->sender.$plist->recieve;
//}
      if (pg_num_rows($pendlist)/*You might need to check whether is is any pending requests*/ ){  
?>
<h2>Pending friend requests</h2>
<?php
      // Write your code here to list the pending requests
      
      while ($row = pg_fetch_object($pendlist) /* You might need to put a while loop condition to list all pending requests */ ){
         $pendname = pg_query_params($dbconn,'select U.uName, U.email from users U, pending P where U.uid = $1' , array($row->sender));
	 $pend =pg_fetch_object($pendname);
	 echo $pend->uname."(".$pend->email.")";
?>

<form action="<?php echo $_SERVER['PHP_SELF'] . '?' . http_build_query($_GET); ?>" method="POST">
<input type="hidden" name="requestor" value="<?php echo $row->sender; ?>"/>
<input type="submit" name="accept_request" value="Accept request"/>
<input type="submit" name="decline_request" value="Decline request"/>
</form>
<?php
        }
?>
<?php
    }
?>
<p/>
<form action="<?php echo $_SERVER['PHP_SELF'] . '?' . http_build_query($_GET); ?>" method="POST">
Email:<br/>
<input name="email" type="text" size="32"/>
<p/>
<input type="submit" name="add_friend" value="Add Friend"/>
<input type="submit" name="remove_friend" value="Remove Friend"/>

</form>
<?php
}
?>
</td>
<tr>
  <td>
    <h2>Message Board</h2>
    <form action="<?php echo $_SERVER['PHP_SELF'] . '?' . http_build_query($_GET); ?>" method="POST">
    <textarea name="message" cols="32" rows="4">
    </textarea>
    <p/>
    <input type="submit" name="create" value="Post message"/>
    </form>
<?php
    if ($_POST['create']) {
        $text = $_POST['message'];
	$datetime = date("d/m/y h:i:s");
	// Write your code here to process a message-create request
       $bins = pg_query_params($dbconn, 'INSERT INTO board(profile,poster,pname,message,datetime) values($1,$2,$3,$4,$5)',array($uidView,$uid,$useron,$text,$datetime)); 
    }
    // Write your code here to list all messages on the wall
       $bget = pg_query($dbconn, "SELECT distinct * FROM BOARD where '$uidView'=profile order by datetime desc");
    while ($bfill = pg_fetch_object($bget) /* while loop condition to retrieve message data from the database */) {
      // Retrieve a message from the database
	$bpost= $bfill->poster;
	$bname= $bfill->pname;
	$bmess= $bfill->message;
	$bdate= $bfill->datetime;
    
        // List all the messages on the wall, we use some hardcoded data here
        // but you should retrieve data from the database in your page
    ?>    <table>
	  <tr><td>By <a href="home.php?profile=<?php echo $bpost?>"><?php echo "$bname"; ?></a> at <?php echo "$bdate"; ?>:</td></tr>
	  <tr><td width=100><?php echo "$bmess"; ?></td></tr>
	</table>
    <?php
    }
?>
  </td>
  <td>
    <h2><a href="logout.php">Log out</a></h2>
  </td>
<?php
// Write your code here to close the connection to the database server
pg_close($dbconn);
?>



</body></html>
