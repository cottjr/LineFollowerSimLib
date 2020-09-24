package LineFollowerSim;


import java.util.ArrayList;
import processing.core.*; //PApplet;



/*
draw overhead view of robot in from fixed perspective "above" robot
collect sensor data

then if courseViewMode (Tab key toggles ON/OFF) overdraw above with
entire course scaled to viewport.

While courseView mode is active, left click mouse updates current and default position of robot.

Ron Grant
May 17, 2020
May 22, 2020  - courseViewMode added
Jun 28, 2020  - updated for addition of Sensor tab methods
                moved all sensor code to Sensor, updateSensors() called after overhead robot view rendered
                to screen (where robot center is center of screen and heading toward "top of screen")

Aug 11, 2020  - think this is when I added robot heading display and mouse drag to 
                course view

Aug 18, 2020  - added mouse (left button - press and hold) drag in robot view 
                in addition to (right button - press and hold) rotate robot

Sept 19, 2020 - integrating into library
                moving things around... ugh
                creating new classes e.g. View ... ugh
                   
                

*/


class View {  // no modifier = package protected 

 
  PApplet p;
  
  public boolean r3D = true; 
  
  public VP robotVP;   // robot view (on screen viewport)
  public VP courseVP;  // course view (on screen viewport)
  
  /**
   *  identical to robot view, except scaling 1:1
   */
  public VP sensorVP; // robot view for sensor read  - may or may not be final view 
    
  
  // cookie crumbs dropped by robot. Giving user access to cookie crumbs might be an issue
  // they are "blindly placed by the robot as it runs", but the current actual robot x,y location (from standpoint of simulation) is used 
  // to encode their position.  One thought if a robot ran a contest "slow" with success, it could reference cookie crumb list to run fast.
  // Fair game if robot controller generates its own crumbs, but questionable to allow access to this crumb list.
  
  private ArrayList <PVector> crumbList = new ArrayList <PVector> ();   

 
  private float curCrumbX;
  private float curCrumbY;
 
  public float crumbThresholdDist;     // cookie crumbs - used for path tracking
  public  boolean crumbsEnabled;    
 
  public boolean drawRobotCoordAxes = true;  // set false to hide robot coordinate axes 
  
  
  // mouse drag state vars 
  
  int  headingChangeX = -999; // used to log mouseX on right mouse press and hold and drag in X to change heading
  float headingChangeStart;   // used to log robot heading upon right mouse press

  // mouse drag robot while in robot view state vars
  int startDragX = -999;
  int startDragY;
  float startDragLocX,startDragLocY;

  int courseDPI = 64;           // LFS can change via call, sets this value too.                                  
   
  View(PApplet parent)
  {
    p = parent;
    this.courseDPI = courseDPI;
    crumbThresholdDist = 0.5f;     // distance from previous crumb must exceed this value before new crumb is generated
  }
  
  
  
  void setCourseDPI(int dpi) { courseDPI = dpi; }    // called by LFS
	
  /**
   *  sets up transform for user draw functions, called from user code.
   */
  void setupUserDraw()  
  {
    p.resetMatrix();
    p.camera();
    
    float sx = courseDPI * robotVP.w / sensorVP.w;
    float sy = courseDPI * robotVP.h / sensorVP.h;
    
    p.translate (robotVP.x+robotVP.w/2,robotVP.y+robotVP.h/2);
    p.scale (sx,sy);
    p.rotate(-PApplet.PI/2.0f);      // -90 degrees   +Y to right and +X up (in window)
    p.strokeWeight(4.0f/courseDPI);  // 2 pixels at current scale 
  }
  
  
  /** Define robot view display region in screen coordinates. This viewport is  
   *  scaled version of 64 DPI frame buffer rendered for robot sensor pixel sampling. 
   *  <p> 
   *  TBD - might be requirement that aspect ratio is maintained. Might consider only width
   *  parameter with auto calculation of height if this is the case. - RDG Sept 19, 2020.
   * 
   * 
   * @param x          upper left corner x
   * @param y          upper left corner y
   * @param width      viewport width 
   * @param height     viewport height 
   */

  public void defineRobotViewport (int x, int y, int width, int height)  
  {
	robotVP = new VP(x,y,width,height);  
  }
  
  /** Define sensor view display (like robot view) region in screen coordinates. This viewport is  
   *  1:1 version of 64 DPI frame buffer rendered for robot sensor pixel sampling. 
   * 
   * 
   * @param x          upper left corner x
   * @param y          upper left corner y
   * @param width      viewport width 
   * @param height     viewport height 
   */

  public void defineSensorViewport (int x, int y, int width, int height)  
  {
	sensorVP = new VP(x,y,width,height);  
  }
  
  
  /** Define course view display region in screen (pixel) coordinates.
   * 
   * @param x        upper left corner x
   * @param y        upper left corner y 
   * @param width    viewport width 
   * @param height   viewport height 
   */
  
  public void defineCourseViewport (int x, int y, int width, int height)  
  {
	courseVP = new VP(x,y,width,height);  

  }
   

//used for UV coordinate translation and rotation to map from course image texture to 
//robot viewport 

void vertTranslate (PVector[] v,float x, float y)
{
  for (int i=0; i<v.length; i++)
  {
    v[i].x += x;
    v[i].y += y;
  }
}

void vertRotateScale (PVector[] v,float theta,float scaleX, float scaleY)  // 2D rotation 
{
  float ca = PApplet.cos(theta);
  float sa = PApplet.sin(theta);
  
  for (int i=0; i<v.length; i++)
  {
    float x = v[i].x;
    float y = v[i].y;
    v[i].x = (x*ca-y*sa)*scaleX;
    v[i].y = (x*sa+y*ca)*scaleY;
  }
}

  
public void drawSensorView(PImage courseImage, Robot robot, int courseDPI)  // draw robot view for sensor reading at 1:1 scale (64 DPI)
{
  int w = sensorVP.w;
  int h = sensorVP.h;
  
  PVector[] v = new PVector[4];
  
  v[0] = new PVector(0,0);          // sample region of course bitmap 
  v[1] = new PVector(h,0);          // 1:1 course bitmap is 64DPI and we want to sample 64DPI image
  v[2] = new PVector(w,h);
  v[3] = new PVector(0,h);

  // calculate position and orientation of u,v coordinates to sample course texture map
  
  vertTranslate(v,-w/2,-h/2);                              // translate center of view to origin
  vertRotateScale(v,PApplet.radians(robot.heading-90),1.0f,1.0f);   // rotate (about origin)  and scale e.g. 2.0 doubles sample region size
 
 
  vertTranslate(v,robot.x*courseDPI,robot.y*courseDPI);    
  
  PShape rv;
  
  rv = p.createShape();
  rv.beginShape();
  
  rv.texture(courseImage);
  
  rv.textureMode(PApplet.IMAGE);              // uv coordinates in image pixels not normalized
 
 
  rv.vertex(0,0,0, v[0].x,v[0].y);      // xyzuv
  rv.vertex(h,0,0, v[1].x,v[1].y);
  rv.vertex(w,h,0, v[2].x,v[2].y);
  rv.vertex(0,h,0, v[3].x,v[3].y);
  
  
  //rv.vertex(w,0,0, v[1].x,v[1].y);
  //rv.vertex(w,h,0, v[2].x,v[2].y);
  //rv.vertex(0,h,0, v[3].x,v[3].y);
  
  rv.endShape();
  
  p.resetMatrix();  // temp ?
  p.camera();
  
  p.translate(sensorVP.x,sensorVP.y);       // offset window origin 
  
  p.shape(rv);                              // draw shape to display window  
  
  p.translate(-sensorVP.x,-sensorVP.y);     // undo transform  	  

 
  
}

boolean mouseInside(VP v) 
{    // mouse in viewport test 
	int mx = p.mouseX-v.x;    
	int my = p.mouseY-v.y;
    return (mx>0)&&(mx<v.w)&&(my>0)&&(my<v.h); }  


public void sensorUpdate(Sensors sensors, int dpi)
{ sensors.update(sensorVP,dpi);    // process robot view  (64DPI overhead view of invisible robot)
}


public void coverSensorView(int r, int g, int b, int a)    // color including alpha 
{
  p.resetMatrix();
  p.camera();
  p.pushStyle();
  p.stroke (r,g,b,a);
  p.fill(r,g,b,a);
  p.rectMode(PApplet.CORNER);
  p.rect(sensorVP.x,sensorVP.y,sensorVP.w,sensorVP.h);
  p.popStyle();
	
}
        
  
  /** Draw overhead view of robot, from perspective of orthographic camera mounted directly above
   * robot looking straight down using robots current x,y location and heading on line following course
   * image.
   * <p>
   * @param courseImage
   * @param courseDPI
   * @param robot
   * 
   */
  
  public void drawRobotView(PImage courseImage, int courseDPI, Robot robot)
  {

	  int w = robotVP.w;
	  int h = robotVP.h;
	  
	  float scale = 2.0f;   // need to calc!   for now  400x400 = 1/2 800x800  scale 2.0 x
	  
	  PVector[] v = new PVector[4];
	  
	  v[0] = new PVector(0,0);          // sample region of course bitmap 
	  v[1] = new PVector(w,0);          // 1:1 course bitmap is 64DPI and we want to sample 64DPI image
	  v[2] = new PVector(w,h);
	  v[3] = new PVector(0,h);

	  // calculate position and orientation of u,v coordinates to sample course texture map
	  
	  vertTranslate(v,-w/2,-h/2);                          // translate center of view to origin
	  vertRotateScale(v,PApplet.radians(robot.heading-90),scale,scale);   // rotate (about origin)  and scale e.g. 2.0 doubles sample region size
	 	 
	  vertTranslate(v,robot.x*courseDPI,robot.y*courseDPI);    
	  
	  PShape rv;
	  
	  rv = p.createShape();
	  rv.beginShape();
	  
	  rv.texture(courseImage);
	  
	  rv.textureMode(PApplet.IMAGE);             // uv coordinates in image pixels not normalized
	  
	  rv.vertex(0,0,0, v[0].x,v[0].y);      // xyzuv
	  rv.vertex(w,0,0, v[1].x,v[1].y);
	  rv.vertex(w,h,0, v[2].x,v[2].y);
	  rv.vertex(0,h,0, v[3].x,v[3].y);
	  
	  rv.endShape();
	  
	  p.resetMatrix();  // temp ?
	  p.camera();
	  
	  p.translate(robotVP.x,robotVP.y);       // offset window origin 
	  
	  p.shape(rv);                            // draw shape to display window  
	  
	  p.translate(-robotVP.x,-robotVP.y);     // undo transform  	  
  
	
	  // draw robot coordinate axes 
	   
	  p.resetMatrix();
	  p.camera();
	
	  	 
	  setupUserDraw();         // needed for mouse drag?
	  mouseDragRobot(robot); 
	  
  }
  

void drawRobotCoordAxes()  // called from LFS 
{
  p.pushMatrix();
  
  p.resetMatrix();
  p.camera();
 
  int xL = 40;        // x axis length - default
  int yL = 40;
 
  p.translate (robotVP.x+robotVP.w/2,robotVP.y+robotVP.h/2);
 
  p.textSize(32);
  p.strokeWeight(3);
  p.stroke(0,250,0);   // green
  p.line (0,0,yL,0);   // screen X (y axis line)
  p.fill (0,250,0);
  p.text ("Y",yL,4);
 
 
  p.stroke(250,0,0);   // red 
  p.line (0,0,0,-xL);  // screen Y (x axis line) 
  p.fill (250,0,0);
  p.text ("X",-6,-xL);
  
  p.popMatrix();
  
} 

 
  // at present drawCourse view using transform applied to scaled course image
  // shape probably faster
  
  void drawCourseView(PImage course, Robot robot, int courseDPI, boolean rotateCourse90,boolean contestFinished)
  {
    int cw = course.width;     // width and height of course image in pixels
    int ch = course.height;
	    
	float vs = 1.0f;
	float vsx = 1.0f*courseVP.w/cw;
	float vsy = 1.0f*courseVP.h/ch;
	    
	if (rotateCourse90)
	{
	  vsx = 1.0f*courseVP.w/ch;
	  vsy = 1.0f*courseVP.h/cw;
	}
	    
	if (vsx<vsy) vs = vsx;                  // calculate uniform view scale that will fit course into viewport 
    else vs = vsy;
	      
    float sx = vs*courseDPI;   // now uniform scaling        viewport pixels to inches multiplier 
    float sy = sx; 
	       
	
	int vpx = courseVP.x;        // course viewport origin
	int vpy = courseVP.y;
	
	if (rotateCourse90)
	  vpy  += courseVP.h;                  // make room at window top for status   
	
	
	p.pushMatrix();
	
	p.resetMatrix();
	p.camera();
	    
	if (rotateCourse90)
	{
	   float tx = ch*vs + courseVP.x;  // course height (now screen width scaled to view) + screen horz offset
	      
	   p.translate (tx,courseVP.y);    // origin at upper right 
	   p.rotate (PApplet.radians(90.0f));    // +cw -ccw 
	   p.scale(vs,vs);
	   p.image (course,0,0); 
	}
	else // non-rotated case - simply scale course and translate origin to course view location on screen
	{ 
	  float tx = courseVP.x;        // place image origin at screen XY = horzOffsetCV,viewsTopBorder
	  p.translate (tx,courseVP.y);  
	  p.scale(vs,vs);                   // image scale factor calculated to scale image to fit into viewport
	                                    // which is widthCV by heightCV pixels 
	  
	  p.image (course,0,0);             // draw course with no offset (transform composition above does the work)
	} 
	
    p.popMatrix();
	

    // note: appended mouse drag code to draw course view
    //
    //public void mouseDragRobotInCourseView(Robot robot, float sx, float sy, boolean rotateCourse90)
    //{
    //  may want to restrict this code during contest run -- or at least set a flag.  robotHasBeenTouched...
  	  
    if (mouseInside(courseVP))  // this method is likely dependent on no mouse button clearing drag state in mouseDragRobot()
   	{  
      if (p.mousePressed && (p.mouseButton == PApplet.LEFT))  // allow moving around on course
  	  {                                                 // e.g. used to determine location of start then
  	                                                   // hardcoded into robot position
  	    float newX,newY;
  	    
  	    if (rotateCourse90)
  	    {
  	      float wcp = 1.0f*course.height*vs;   // course horizontal width screen pixels    
  	      newY  = (courseVP.x+wcp-p.mouseX)/sy;
  	      newX  =  (p.mouseY-courseVP.y)/sx;
  	    }
  	    else
  	    {
  	      newX = (p.mouseX-vpx)/sx;
  	      newY = (p.mouseY-vpy)/sy;
  	    }  
  	    
  	     
  	    // update robot location and also default location using mouse click location.
  	   
  	         
  	    robot.setCurrentAndInitialLocationAndHeading(newX,newY,robot.heading);
  	    robot.hardStop();
  	         
  	    //PApplet.println (String.format ("Robot New Position (%1.1f,%1.1f)",robot.x,robot.y));
  	  
  	 }  
   	}  
    
    p.resetMatrix();
    p.camera();

    // draw pointer in robot heading direction
    float x,y;
       
    if (rotateCourse90)
    {
      float wcp = course.height*vs;   // course horizontal width screen pixels      
      x = courseVP.x + wcp - robot.y * sy;
      y = robot.x * sx + courseVP.y;
    }
    else
    {
      x = robot.x * sx + vpx;
      y = robot.y * sy + vpy;
    }
    
    
    
    float a = PApplet.radians(robot.heading);
    float r = 60.0f;
    p.strokeWeight(12);
    p.stroke (50,50,255);
    p.translate(x,y);
    if (rotateCourse90) a += PApplet.radians(90.0f); 
    float xe = -r*PApplet.cos(a);
    float ye = -r*PApplet.sin(a);
    
    p.line (0,0,xe,ye);
    p.ellipseMode (PApplet.CENTER);
    p.ellipse (0,0,10,10);
    
    p.translate (xe,ye);
    
    p.resetMatrix();
    p.camera();          // in test with P3D not resetting camera slowed down frame rate???
    
    // draw the cookie crumbs -- R)Reset clears list  
    
    float xc = 0;
    float yc = 0;
    boolean firstCrumb = true;
    
    p.stroke (0,255,0); // crumb color  
    p.strokeWeight(3.0f);
    for (PVector pt : crumbList)
    {
      
     // scale from course coordinates in inches to normalized coordinates,
     // enclosed in () then scale to screen
      
    if (rotateCourse90)
    {
      float wcp = course.height*vs;   // course horizontal width screen pixels    
      
      xc =  courseVP.x + wcp- (pt.y * sy);  // x  upper right corner of course image  - y scaled to pixel units
      yc =  courseVP.y + (pt.x * sx);   // y  top border offset  + x scaled to pixel units  
     
    }  
    else
    {  xc =  vpx + (pt.x * sx);    // horzoffset + x scaled to pixels
       yc =  vpy + (pt.y * sy);    // vertoffset + y scaled to pixels
    }  
             
    p.point (xc,yc);
     
    if (firstCrumb) {
       
       p.pushStyle();
       p.fill (20,200,20);
       p.textSize (20);
       p.text ("(S)",xc,yc);
       p.popStyle();
       
       firstCrumb = false; 
    }
     
      
  }
    
  
    if (contestFinished)
    {
      p.pushStyle();
      p.fill (200,20,20);
      p.textSize (20);
      p.text ("(F)",xc,yc);
      p.popStyle();
    }  
   
    
    
    p.strokeWeight(1.0f);
   
   } // end draw course view 
  
  
  

  /**
   *  Add crumb if distance to previous crumb greater than crumbThresholdDist 
   */
  
  public void crumbAdd(Robot robot) 

  {

  // add cookie crumb if robot has moved more than crumbThresholdDist from previous crumb
  if (PApplet.dist(robot.x,robot.y,curCrumbX,curCrumbY) > crumbThresholdDist) 
  {
   crumbList.add(new PVector(robot.x,robot.y));
   curCrumbX = robot.x;
   curCrumbY = robot.y;
  }


} // end addCrumb

  
  /**
   *  Clear all crumbs in course view
   */
  
  public void crumbEraseAll() { crumbList.clear(); }
  
  
 
  /** Handle left mouse press and drag in robotVP or courseVP move robot.
   *  Handle right mouse press and drag to rotate robot. 
   *  !!! TBD - don't allow during contest run / or flag run as robot moved during run
   *  Need to wrap this method in LFS method - robot is not accessible to user code 
   *  
   * @param robot  
   */
  
  public void mouseDragRobot(Robot robot)
  {
    if (p.mousePressed && (p.mouseButton == PApplet.RIGHT))  // right mouse down and move horizontally to change robot heading
    {
     if (headingChangeX == -999)  // right press just started, log mouse X location
     {
      headingChangeStart = robot.heading;
      headingChangeX = p.mouseX;
      
      if (!mouseInside(robotVP)&& !mouseInside(courseVP))
        headingChangeX = -999;  // cancel -- mouse must start in view  
      
     }  
      if (headingChangeX != -999)
      {
        robot.heading = headingChangeStart + p.mouseX-headingChangeX;         
        if (robot.heading > 360) robot.heading -= 360;
        if (robot.heading < 0) robot.heading += 360;
        robot.headingi = robot.heading;   // make new heading default heading if R-Restart
      }  
  }
    
  if (!p.mousePressed)          // when mouse released, reset heading change state
     headingChangeX = -999;
      


  if (mouseInside(robotVP))	  
  {
   if (p.mousePressed && (p.mouseButton == PApplet.LEFT))  // allow moving around on course
   {                                                 // e.g. used to determine location of start then
                                                     // hardcoded into robot position
      if (startDragX == -999)
      {                              // start of press - record mouse XY and robot XY
       
        if (mouseInside(robotVP)|| mouseInside(courseVP))
        {
          startDragX = p.mouseX;    
          startDragY = p.mouseY;
          startDragLocX = robot.x;
          startDragLocY = robot.y;
        }  
      }
      
      if (startDragX != -999)
      {
        float dx = (p.mouseX-startDragX)*0.02f;     // calculate mouse XY displacment since press
        float dy = (p.mouseY-startDragY)*0.02f;     // in mouse XY (move to right X+) move up  Y-
        
         
        // convert mouse XY drag displacements to match robot's most often rotated view of the
        // world. First, I started with robot heading 0 where 
        // robot pointing in world -X direction with -Y world to right
        // in this case positive mouse Y displacement (drag mouse toward edge to desk)
        // (dy>0) results in decrease in world X where course image moves "down" on screen
        // as mouse does viewing from above.
        //
        // Now as robot heading is changed world rotates around robot. To make dragging of 
        // mouse match screen image, mouse XY drag offsets must be resolved into changes is robots 
        // world x,y location.
        // 
        // using 2D rotation formula
        // e.g. given c=cos(theta) s=sin(theta)  
        // rotate xy through angle theta with respect to x axis (where xr yr are rotated xy)    
        // xr = x*c-y*s
        // yr = x*s+y*c
        
        // perform rotation of mouse displacement vector (note: swapped x,y)
        // to make mouse movements drag world appropriately         
       
        // mouse coordinates   o--->X+   move to right increasing X
        //                     |
        //                     Y+   move "down" increasing Y
        
        // world coordinates   upper left hand image corner is origin 
        //                     positive X moving to right, positive Y moving down
        
        // robot initial heading 0          point toward world -X  directon 
        //    to right of robot heading 90  is in world -Y direction 
       
        // as robot rotates clockwise to right increasing heading angle,
        // world appears to rotate left (ccw)
        
        // hence I believe this is reason I reversed the robot.heading angle in the 
        // mouse to world rotation.
              
        float c = PApplet.cos(-PApplet.radians(robot.heading));
        float s = PApplet.sin(-PApplet.radians(robot.heading));
           
        float xw = - ( dy * c - dx * s);
        float yw = dy * s + dx * c;
       
        // update robot location             
              
        robot.x = startDragLocX + xw;
        robot.y = startDragLocY + yw;
        
      } // end if startDragX != -999   
         
    } else
       startDragX = -999;  // reset drag state
  } // end if !courseView     
 
 
  // overdraw robot view with course image - if in course view mode
  // or draw to right of robot view if dualView 
  
  }
  
} // end class 

  