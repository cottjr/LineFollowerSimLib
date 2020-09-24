
/* Method userControllerResetAndRun is called when the simulation
   is starting or re-starting your robot.
   
   It will have cleared sensor definitions, so best to call userInit()
   Also, anything related to controller state should be reset.
   
   You may want to verify that multiple R)un commands has same behavior 
   as inital run when sketch is started. One possible problem is not zeroing
   variables that are expected to be zero when program starts might result
   in differing behavior.

*/
  
void userControllerResetAndRun() 
{
  userInit();                               // call userInit() to init sensors & accel rates.
  
  if (courseNum == 2)
  lfs.setPositionAndHeading (52,12,0);      // override initial position & heading with start
                                            // at 52,12,0
  
   
  lfs.setTargetSpeed(6.0f);   // example start driving robot straight
  // might be used if you are only controlling turn rate.
  
  // reset user state variables
  // (not much to do in simple demo controller)
  // If you are creating a challenge course controller you will probably have several state variables.
  // Be sure to reset them were 
  
  ePrev = 0;

} // end userControllerResetAndRun


float ePrev;      // global previous error 


void userControllerUpdate ()    
{
// your robot controller code here - this method called every time step
// crude attempt to control velocity as function of current centroid error 

float[] sensor =  sensor1.readArray();   // returns reference to sensor array of floats  wjk 7-6-20  

float e = calcSignedCentroidSingleDarkSpan(sensor) ;   // error in pixels of line intersection with sensor
//println(String.format("line positioning error %3.1f",e)); 


ePrev = e;   

// note you can dynamically change sensor positions if you wish
// in this example sensor radius is varied - just to show it can be done

// sensor1.setRotation(10);  // now theta
//sensor1.setArcRadius(1.0+ (frameCount%100)/100.0);
//sensor1.setRotation (90*mouseX/width);

// generally your robot will up updating TargetTurnRate and possibly TargetSpeed

lfs.setTargetTurnRate(-e * 10 + (e - ePrev) * 10.0f);   // turn rate in degrees per second
lfs.setTargetSpeed(1.0f + abs(12.0f/(abs(e/2.0f)+1.0f)));   

//lfs.setTargetSpeed (4.0f);


}

//very simple line detector, given sensor array from line (or 1/2 circle) sensor
//you will want to enhance the line detector if you are creating a challenge course robot

float calcSignedCentroidSingleDarkSpan(float[] sensor)  // 0=line centered under sensor array  + to right , - to left
{

int n = sensor.length; // total number of sensor samples
// calculate centroid of single black line 

float sum = 0;
int count = 0;
float centroid = 0;
for (int i=0; i<n; i++)
if (sensor[i]<0.5) { sum+= i;  count++; }
if (count>0)
centroid = (0.5f*n)-(sum/count);  // make centroid signed value  0 at center

return centroid; 
}
