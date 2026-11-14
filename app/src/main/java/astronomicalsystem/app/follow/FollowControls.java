package astronomicalsystem.app.follow;

/**
 * Operations the user can perform to choose what the camera follows: the barycentre first, then
 * the bodies in the order of the simulation, and the barycentre again.
 */
public interface FollowControls {

    void followNext();

    void followPrevious();
}
