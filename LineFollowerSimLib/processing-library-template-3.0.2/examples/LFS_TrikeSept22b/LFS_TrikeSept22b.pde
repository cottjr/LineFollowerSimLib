/* Line Following Simulator User Application - Processing Environment

   Sketch files 
 
   .pde file  description and standard methods() called by simulator 
 
   LFS_XXX    Sketch main program - high level boiler plate code 
              can be optinally modified to suit user needs
   
   UserCon    User controller code 
                userControllerResetAndRun()   method called by simulator to start robot running
                userControllerUpdate()        method called by simulator at each time step
                
   UserDraw   optional - add features to overlay on robot view
                userDraw()                    method called by simulator (optional) to draw
                                              robot features or other information onto robot view
                                              as an overlay
                                              
   UserPanel  display status on screen
                userDrawPanel()
   UserInit   define sensors, acceleration / deceleration rates, max speed, and max turn rate
                userInit()
                
   UserKey    command key decoder 
                keyPressed()                method called when keypress detected
                                            add commands that might be useful
                                            
                                            
   Note: When lfs.setContestMode(true) is set, user code will be unable to alter robot
         location without stopping contest run 
  
  
   Sept 21 to do
   
   Speed UP !!!  21 fps trash! ---   Done! 90-150 fps on GTX1060 i7 8700
   
   Don't allow drag during contest run
   Help - display   mouse info 
   Show sim maximums 
   Better   Run SPACE Stop    Finish - prompt for comment 
   
   UserDraw - maybe consider overlay on 1:1 robot view if small view not
              present.
   
   show sensors 
  
  
*/  
  


import LineFollowerSim.*;  // Simulator API - accepts turn rate and speed commands 
                           // tracks virtual robot position and heading
                           // renders robot and course views, reads sensor data

LFS lfs;  // line following simulator - single instance created in setup()

// pull into lib - hide implementation details 

int simSpeed = 9;                     // simulation speed 0=single step 1 slow to 9 normal  -- controlled by Keypress           
boolean simFreeze = false;            // toggled by space bar (when simSpeed>0) else single step -- controlled by Keypress
boolean simRequestStep = false;       // program has decided it needs to take a simulation step
int panelDisplayMode = 2;

boolean showFPS = true;  // show frames per second being drawn - does not affect robot 
                          // stopwatch time, or simulation behavior, just real time
                          // ideally 60 fps will run robot actual speed with default time step
                          // equal to 1/60th secm, 30fps robot appears to run 1/2 speed - slow
                          // motion - as would be noted by slow stop watch.
   
boolean courseTop = true;   // couse view top or bottom                     
                     
//PImage ci;
float fr;   // frame rate 
    
void clearScreen() { background(0,0,20); } // called initally and  when changing course orientation     
    
public void setup()
{
  size (1800,900,P3D);       // window width,height in pixels
  frameRate(8*120);
  
  lfs = new LFS(this);   // single instance of LFS  ref to this applet, robotView width,height e.g. typical 800,600
  
  lfs.defineRobotViewport(40,70,400,400);
  lfs.defineCourseViewport(480,70,1280,640);
 
 
    
  userInit(); // called here - and also on robot start (contest start)
  
  //for (SpotSensor ss : lfs.sensors.spotSensorList) println (ss.getXoff(),ss.getYoff(),ss.read());
  
  simSpeed =9;
  
  clearScreen();
  
 // ci = lfs.getCourse();
  
  lfs.setShowSensorsOnSensorView(true);  // slight frame rate gain if false 
}



  public void draw ()  // method called by Processing at 30 to 60 times per second (1 frame time)
  {
   // Screen not erased upon entry to draw, e.g. with background() method call.
   
   // Robot view and Course view overwrite viewport areas - but not every frame unless view rate dividers set to 1 (see below)
   // Other screen areas should be overwritten with solid rect before writing text.
   
   int alpha = 90;
   if (courseTop) alpha = 219;
   
   if (courseTop)
   lfs.updateSensors(0,0,0,alpha);     // draws 64 DPI bitmap of current robot location on screen (can be covered)
                                       // sensor updates, making sensor data available for user controller
    
   lfs.drawRobotAndCourseViews(1,1,rotate90);  // draw robot and course, using frame divider
   
   // Frame divider (1,1,.. ) used for display every frame. Normal case.
   // To attempt to improve performance read on:
   // robot view rate divider, course view rate divider
   // 0=disable 1=every frame 2=every other frame, 3= every 3..
   // when mouse pressed and not disabled, LFS will temporarily insure every frame
   // Using GPU may eliminate this feature... 
    
   if (!courseTop) lfs.updateSensors(0,0,0,alpha); 
     
   if (courseTop) userDraw();
    
    resetMatrix();
    camera();
    
       
    rectMode (CORNER);   // text boxes - backgrounds 
    fill (0);
    rect (40,10,400,48);
    rect (480,10,1200,48);
       
    if (showFPS)   // draw() frames per sec (fps)  - performance monitor 
    { textSize (18);
      fill(150);
      if (frameCount%60 == 0) fr = frameRate;
      text(String.format("%2.0f fps",fr),340,55);
    }
    
    fill(240);
    textSize(28);
    text(lfs.getContestTimeString(),51,46);
    
    if (lfs.contestIsRunning())
      text("Run",width-248,47);
      
    text(lfs.nameFirst+" "+lfs.nameLast+"  "+lfs.nameRobot,500,46);  
    
     
     
    
    lfs.drawRobotLocHeadingVel(974,47);        // draw x,y location and heading bitmap (values not available) 
                                               
    
    
   if (!simFreeze)                             // if simulation not frozen with speed=0, key='0' freezes
   {
     if (simSpeed == 0)
     { }
     else
     if (simSpeed == 1)
     { if ((frameCount % 60) == 0)             // 1 is really slow   e.g. about frame per second typical
       simRequestStep = true;
     }  
     else 
     if (simSpeed==9) simRequestStep = true;
     else
     if ((frameCount % (60-simSpeed*60/9) == 0))   // allow keys 1..9 to control speed  
       simRequestStep = true;                      // simSpeed of 9 requests frame every time
   }
     
   if (lfs.controllerIsEnabled())    // if turned off, no update.
     userControllerUpdate();         // user reads sensors changes target speed and turn rate as needed.
    
   
   lfs.driveUpdate(simRequestStep);  // if step requested update robot position with current speed and turn rate
                                     // also stopwatch tick is counted. Note: if controller is not enabled 
                                     // this call will insure robot position is updated allowing manual drive.                                             
  
  
   simRequestStep = false;           // ramping speed and/or turn rate toward targetSpeed and targetTurnRate
                                     // using defined acceleration/deceleration rates.
       
   userDrawPanel();
    
   lfs.contestScreenSaveIfRequested();   // generates screen save upon contest "Finish"   
   
  } // end of draw()

 
