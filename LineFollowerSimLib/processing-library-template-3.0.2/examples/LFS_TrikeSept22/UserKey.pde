
//processing calls this method when a key is pressed  
//commands here to allow manual drive of robot and turning drive controller on/off
//
//Note:  Remember to click on the run window to give focus before pressing keys 

String keySummaryConOn  = "SPACE=freeze/step 0..9 speed C)ontroller OFF S)top R)un F)inish";
String keySummaryConOff = "C)ontroller turn on  <- -> turn, up/dn arrow velocity S)top R)un F)inish"; 

boolean rotate90 = true;  // course view  toggle rotation with Alt key 


//0      single step mode
//1..9   simulation speed 1=slowest to 9=normal (0 = single step)
//SPACE  toggle controller run/freeze   in single step mode, take a step
//     if controller not enabled - stops robot
//C     toggle controller ON/OFF

//S     stop robot motion, disable controller 


public void keyPressed()  // handle keypress events for manual driving of robot.
{
  if ((key>='a')&&(key<='z')) key -=32; // shift to uppercase  
 
  if ((key >= '0') && (key <= '9')) 
  { simSpeed = key-'0';
    simFreeze =  (simSpeed == 0);
  }


  
  if (key == 'C') { lfs.setEnableController(!lfs.controllerIsEnabled());   // toggle allowing controller to update
                    if (!lfs.controllerIsEnabled()) lfs.stop();             // position and heading of robot
                  }                                                       // if controller not enabled - stop robot
                      
  if (key == ' ' ) { if (lfs.contestIsRunning()) 
                       lfs.contestStop();
                     else
                     {
                       if (simSpeed == 0) simRequestStep = true;
                       if (simSpeed > 0) simFreeze = ! simFreeze;
                     }  
                   }  
                                                                  
  if (key == 'F')  lfs.contestFinish();                                                                
                                                                  
  if (key == 'P') panelDisplayMode = (panelDisplayMode + 1) % 3;  // cycle display status command panel opacity
   
  
  if (keyCode ==  UP)  lfs.changeSpeed(1.0f);
  if (key ==  'S' )    { lfs.stop(); lfs.setEnableController(false); }
  if (keyCode == DOWN) lfs.changeSpeed(-1.0f);
    
    if (keyCode == LEFT)  lfs.changeTurnRate(-11.25f);
    if (keyCode == RIGHT) lfs.changeTurnRate(11.25f);
  
   if (keyCode == TAB) courseTop = !courseTop;
   
   if (keyCode == ALT) { rotate90 = !rotate90; clearScreen(); }
  
    if (key == 'G') 
    {
       lfs.clearSensors();
       userControllerResetAndRun();
       lfs.setEnableController(true);
       lfs.crumbsEraseAll();
       simFreeze = false;
       
         // enable controller, clear crumbs, reset stopwatch 
    }      
 
    if (key == 'R') 
    {
       lfs.clearSensors();
       userControllerResetAndRun();
       
       lfs.contestStart();     // enable controller, clear crumbs, reset stopwatch
       simSpeed = 9;
       simFreeze = false;
    }                             
}   
   
