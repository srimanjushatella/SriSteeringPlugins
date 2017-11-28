/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package boid.sea;

import static buckland.ch3.ParamLoader.Prm;
import buckland.ch3.boid.Predator;
import buckland.ch3.boid.Prey;
import buckland.ch3.boid.SteeringBehavior;
import buckland.ch3.boid.Vehicle;
import buckland.ch3.common.D2.Vector2D;
import static buckland.ch3.common.D2.Vector2D.Vec2DNormalize;
import static buckland.ch3.common.D2.Vector2D.add;
import static buckland.ch3.common.D2.Vector2D.mul;
import static buckland.ch3.common.D2.Vector2D.sub;
import static buckland.ch3.common.misc.Cgdi.gdi;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author TSM
 */
public class Shark extends Predator {

    public final double PERIPHERAL_VISION = 36;
    public final double PERIPHERAL_VISION_RADIANS = Math.acos(PERIPHERAL_VISION);
    protected Font titleFont = new Font("Arial", Font.BOLD, 16);
    protected double dKillzone = -1;
    protected int killscore = 0;
    //Last count of prey within the killzone
    protected int iLastLunchCount = -1;
    //Calculated heading in degrees
    protected double dTheta;
    //Set this flag to true if debugging
    public final boolean DEBUGGING = true;
    protected List<Vehicle> pPreyList;
    private double SPEED_REFACTOR = 0.50;
    protected float[] aDash1 = {10.0f};
    protected BasicStroke pDashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10.0f, aDash1, 0.0f);
    protected double dSpeed;

    protected List<Predator> PredatorList;

    /**
     * constructor
     *
     * @param spawnPos Initial spawn position
     */
    public Shark(Vector2D spawnPos) {
        super(spawnPos);

        this.dKillzone = Prm.ViewDistance;
    }

    /**
     * This method gets invoked every cycle during the render phase of the game
     * world
     *
     * @param bTrueFalse
     */
    public void Render(boolean bTrueFalse) {
        super.Render(bTrueFalse);
        //convert the headng to a string
        String sHeading = String.format("%4.1f", dTheta);

        //get the graphhcs context
        Graphics2D g = gdi.GetHdc();
        g.setFont(titleFont);

        //setting the colour to black
        g.setColor(Color.BLACK);

        //drawing the string usng the default font
        ArrayList<Vehicle> pPreyList = (ArrayList<Vehicle>) this.m_pWorld.GetPrey();
        g.drawString("Prey:" + pPreyList.size(), 425, 485);

        drawKillingZone(g);
        drawLunchCount(g);

    }

    /**
     * This method gets invoked every cycle during the update phase of the game
     * world
     *
     * @param dTimeElapsed
     */
    public void Update(double dTimeElapsed) {
        super.Update(dTimeElapsed);

        //Get the heading in degrees
        Vector2D pHeading = this.Heading();

        this.dTheta = Math.atan2(pHeading.y, pHeading.x) * 180 / Math.PI;
        this.dSpeed = this.Speed();

        //This array holds our lunch
        List<Vehicle> pLunch = new ArrayList<>();
        //Get all the prey
        List<Vehicle> pPreyList = (ArrayList<Vehicle>) this.m_pWorld.GetPrey();

        //find out which ones are in the killzone
        for (int i = 0; i < pPreyList.size(); i++) {
            Prey pPrey = (Prey) pPreyList.get(i);
            double dDistance = this.Pos().Distance(pPrey.Pos());
            double dFacing = this.Heading().Dot(pPrey.Pos());
            double relativeHeading = this.Heading().Dot(pPrey.Heading());
            if (dDistance <= dKillzone && dFacing > 0 && relativeHeading < PERIPHERAL_VISION) {
                pLunch.add(pPrey);

                //removes prey if predator kills it
                pPreyList.remove(pPrey);
                killscore++;
            }

            //Track when the dinner count peaks then starts to drop
            int iLunchCount = pLunch.size();

            if (DEBUGGING && iLunchCount != iLastLunchCount) {
                System.out.println("" + iLunchCount);
                iLastLunchCount = iLunchCount;
            }

            if (dDistance <= dKillzone && dFacing > 0) {
                pLunch.add(pPrey);

                pPreyList.remove(pPrey);

                iLastLunchCount++;
            }

            if (DEBUGGING && iLunchCount == 0 && iLastLunchCount != 0) {
                System.out.println("---");
                iLastLunchCount = iLunchCount;
            }

            if (dDistance > dKillzone + 30 && dFacing > 0) {

                double a = 0;
                double b = 0;
                int count = 0;
                Prey prey = pPrey;

                for (Vehicle pVehicle : pPreyList) {
                    double dDist = this.Pos().Distance(pVehicle.Pos());
                    double dFace = this.Heading().Dot(pVehicle.Pos());
                    if (dDist > dKillzone + 30 && dFace > 0) {

                        a += pVehicle.Pos().x;
                        b += pVehicle.Pos().y;
                        count++;
                    }
                }

                prey.Pos().x = a / count;
                prey.Pos().y = b / count;

                this.SetVelocity(Pursuit(pPrey));
                this.SetVelocity(Pursuit(prey));

            }
        }

    }

    public void drawSharkAntenna(double dTheta) {

        // This is where the antenna starts which is known by prey position
        int dX1 = (int) (this.Pos().x + 0.5);
        int dY1 = (int) (this.Pos().y + 0.5);
        double dLength = SPEED_REFACTOR * this.m_vVelocity.Length();

        // The end location of the antenna is knowable using algebra and trig:
        // cos(theta) = deltaX / L = (xEnd - xStart) / L
        // sin(theta) = deltaY / L = (yEnd - yStart) / L
        // So, we can solve for xEnd and yEnd
        int dX2 = (int) (dLength * Math.cos(dTheta) + dX1 + 0.5);
        int dY2 = (int) (dLength * Math.sin(dTheta) + dY1 + 0.5);

        Graphics2D g = gdi.GetHdc();
        g.drawLine(dX1, dY1, dX2, dY2);
        g.setColor(Color.RED);

    }

    public void drawKillingZone(Graphics2D g) {

        Vector2D pHeading = this.Heading();

        drawRing(g);

        double dTheta = Math.atan2(pHeading.y, pHeading.x);
        double dTheta1 = dTheta - PERIPHERAL_VISION * Math.PI / 180;
        double dTheta2 = dTheta + PERIPHERAL_VISION * Math.PI / 180;

        drawSharkAntenna(dTheta1);
        drawSharkAntenna(dTheta2);
    }

    protected void drawRing(Graphics2D g) {
        // Draw the predator at the center of the killzone
        int dX = (int) (Pos().x - dKillzone);

        int dY = (int) (Pos().y - dKillzone);

        int dWidth = (int) (2 * dKillzone);

        int dHeight = dWidth;

        // Draw the killzone with the dashed stroke
        g.setColor(Color.RED);

        Stroke pOldStroke = g.getStroke();

        g.setStroke(pDashed);

        g.drawOval(dX, dY, dWidth, dHeight);

        g.setStroke(pOldStroke);

    }

    protected void drawLunchCount(Graphics2D g) {
        // Convert the capture number to a string
        String sLunch = String.format("%3d", killscore);

        // Set the color to red
        g.setColor(Color.RED);

        // Draw the capture number using the default font
        int dPosX = (int) Pos().x;
        int dPosY = (int) Pos().y;

        g.drawString(sLunch + " ", dPosX + 10, dPosY + 10);
    }

    /**
     * this behavior creates a force that steers the agent towards the evader
     */
    private Vector2D Pursuit(final Vehicle evader) {
        //if the evader is ahead and facing the agent then we can just seek
        //for the evader's current position.
        Vector2D ToEvader = sub(evader.Pos(), this.Pos());

        double RelativeHeading = this.Heading().Dot(evader.Heading());

        if ((ToEvader.Dot(this.Heading()) > 0)
                && (RelativeHeading < -0.95)) //acos(0.95)=18 degs
        {
            return Seek(evader.Pos());
        }

        //Not considered ahead so we predict where the evader will be.
        //the lookahead time is propotional to the distance between the evader
        //and the pursuer; and is inversely proportional to the sum of the
        //agent's velocities
        double LookAheadTime = ToEvader.Length()
                / (this.MaxSpeed() + evader.Speed());

//        LookAheadTime += TurnaroundTime(m_pVehicle, evader.Pos());
        //now seek to the predicted future position of the evader
        return Seek(add(evader.Pos(), mul(evader.Velocity(), LookAheadTime)));
    }

    /**
     * Given a target, this behavior returns a steering force which will direct
     * the agent towards the target
     *
     * @param TargetPos Target position to seek.
     * @return Change in velocity
     */
    public Vector2D Seek(Vector2D TargetPos) {
        //System.out.println(this.Velocity());
        Vector2D DesiredVelocity = mul(Vec2DNormalize(sub(TargetPos, this.Pos())), 70);
        return sub(DesiredVelocity, this.Velocity());
    }

    /**
     * This behavior is similar to seek but it attempts to arrive at the target
     * with a zero velocity
     */
    private Vector2D Arrive(Vector2D TargetPos, SteeringBehavior.Deceleration deceleration) {
        Vector2D ToTarget = sub(TargetPos, this.Pos());

        //calculate the distance to the target
        double dist = ToTarget.Length();

        if (dist > 0) {
            //because Deceleration is enumerated as an int, this value is required
            //to provide fine tweaking of the deceleration..
            final double DecelerationTweaker = 0.3;

            //calculate the speed required to reach the target given the desired
            //deceleration
            double speed = dist / ((double) deceleration.value() * DecelerationTweaker);

            //make sure the velocity does not exceed the max
            speed = Math.min(speed, this.MaxSpeed());

            //from here proceed just like Seek except we don't need to normalize 
            //the ToTarget vector because we have already gone to the trouble
            //of calculating its length: dist. 
            Vector2D DesiredVelocity = mul(ToTarget, speed / dist);

            return sub(DesiredVelocity, this.Velocity());
        }

        return new Vector2D(0, 0);
    }

}
