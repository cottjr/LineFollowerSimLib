

//optional draw to robot view methods 
//supporting subset of processing drawing commands.
//These methods must be included, but may be empty or code commented out.


void circle (float x, float y, float r) // circle, not present in older processing versions 
{
ellipseMode(CENTER);
ellipse(x,y,r*2,r*2);
}



void userDraw()
{
  lfs.setupUserDraw();       // sets up transforms origin robot center scale inches
  lfs.drawRobotCoordAxes();  // draw robot coordinate axes 
  
  strokeWeight(4.0f/lfs.courseDPI);                // line thickness in pixels 
  stroke (color (255,0,255,180));  // r,g,b,alpha (0=transparent ... 255= opaque)
  noFill();
  rectMode(CENTER);
  rect(0,-2.5f,4,0.5f);
  rect(0, 2.5f,4,0.5f);
  circle(0,0,4.0f);                // x,y,d    x+ forward of center y+ right 
  circle(-2,0,1);
  stroke (color(0,0,255,180));
  line(0,-3,0,3);
}
