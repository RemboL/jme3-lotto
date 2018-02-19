package pl.rembol.jme3.tjug;import static com.jme3.math.FastMath.sqr;import java.awt.DisplayMode;import java.awt.GraphicsEnvironment;import java.util.ArrayList;import java.util.List;import com.jme3.app.SimpleApplication;import com.jme3.app.state.AbstractAppState;import com.jme3.bullet.BulletAppState;import com.jme3.bullet.control.RigidBodyControl;import com.jme3.input.KeyInput;import com.jme3.input.controls.ActionListener;import com.jme3.input.controls.KeyTrigger;import com.jme3.light.AmbientLight;import com.jme3.light.PointLight;import com.jme3.math.ColorRGBA;import com.jme3.math.FastMath;import com.jme3.math.Quaternion;import com.jme3.math.Vector3f;import com.jme3.renderer.RenderManager;import com.jme3.renderer.ViewPort;import com.jme3.renderer.queue.RenderQueue;import com.jme3.scene.Spatial;import com.jme3.scene.control.AbstractControl;import com.jme3.system.AppSettings;import pl.rembol.jme3.utils.Materials;public class Main extends SimpleApplication {    private List<Ball> balls = new ArrayList<>();    private int phase = 0;    private boolean windUp = false;    public static void main(String[] args) {        Main app = new Main();        DisplayMode displayMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()                .getDisplayMode();        app.setShowSettings(false);        AppSettings settings = new AppSettings(true);        settings.setWidth(displayMode.getWidth());        settings.setHeight(displayMode.getHeight());        app.setSettings(settings);        app.start();    }    @Override    public void simpleInitApp() {        getFlyByCamera().setMoveSpeed(30);        Spatial chamber = assetManager.loadModel("chamber.blend");        chamber.setQueueBucket(RenderQueue.Bucket.Transparent);        Materials.setAlpha(chamber, "Diffuse", 0.2f);        Materials.setAlpha(chamber, "Ambient", 0.2f);        getCamera().setLocation(new Vector3f(0, 12, 70));        rootNode.attachChild(chamber);        Spatial room = assetManager.loadModel("room.blend");        rootNode.attachChild(room);        AmbientLight ambientLight = new AmbientLight();        ambientLight.setColor(ColorRGBA.White.mult(0.3f));        rootNode.addLight(ambientLight);        PointLight pointLight = new PointLight();        pointLight.setColor(ColorRGBA.White.mult(0.7f));        pointLight.setPosition(new Vector3f(20, 100, 20));        rootNode.addLight(pointLight);        BulletAppState bulletAppState = new BulletAppState();        stateManager.attach(bulletAppState);        bulletAppState.getPhysicsSpace().setGravity(Vector3f.UNIT_Y.negate().mult(20f));//        bulletAppState.setDebugEnabled(true);        RigidBodyControl rigidBodyControl = new RigidBodyControl(0f);        chamber.addControl(rigidBodyControl);        bulletAppState.getPhysicsSpace().add(rigidBodyControl);        for (int i = 0; i < 10; ++i) {            for (int j = 0; j < 8; ++j) {                Ball ball = new Ball(this, rootNode, new Vector3f(-9 + i * 2, 22.7f - j * 2, -25.7f),                        "" + (j * 10 + i + 1));                ball.addControl(new RotateWinningBallsControl());                balls.add(ball);            }        }        stateManager.attach(new WindUpAppState());        inputManager.addMapping("nextPhase", new KeyTrigger(KeyInput.KEY_SPACE));        inputManager.addListener((ActionListener) (name, isPressed, tpf) -> {            if (!isPressed) {                nextPhase();            }        }, "nextPhase");    }    private void nextPhase() {        if (phase == 0) {            phase = 1;            balls.forEach(ball -> ball.getControl(RigidBodyControl.class).setKinematic(false));        } else if (phase == 1) {            phase = 2;            windUp = true;        } else if (phase == 2) {            phase = 3;            windUp = false;        }    }    public class RotateWinningBallsControl extends AbstractControl {        private boolean isInTube = false;        private float motionlessFor = 0f;        private Quaternion winningBallDirection;        @Override        protected void controlUpdate(float tpf) {            if (!isInTube) {                if (FastMath.sqrt(sqr(getSpatial().getWorldTranslation().x - 22) + sqr(                        getSpatial().getWorldTranslation().z - 5)) < 3) {                    isInTube = true;                }            } else {                if (getSpatial().getControl(RigidBodyControl.class).getLinearVelocity().length() < 0.0001f) {                    motionlessFor += tpf;                } else {                    motionlessFor = 0f;                }                if (motionlessFor > 1f) {                    getSpatial().getControl(RigidBodyControl.class).setPhysicsRotation(getWinningBallDirection());                }            }        }        protected Quaternion getWinningBallDirection() {            if (winningBallDirection == null) {                winningBallDirection = new Quaternion();                winningBallDirection.lookAt(Vector3f.UNIT_X, Vector3f.UNIT_Y);            }            return winningBallDirection;        }        @Override        protected void controlRender(RenderManager rm, ViewPort vp) {        }    }    public class WindUpAppState extends AbstractAppState {        public void update(float tpf) {            balls.forEach(ball -> {                if (windUp) {                    if (FastMath.sqrt(sqr(ball.getLocalTranslation().x) + sqr(                            ball.getLocalTranslation().z)) < (20 - ball.getLocalTranslation().y) / 4) {                        ball.getControl(RigidBodyControl.class).applyCentralForce(Vector3f.UNIT_Y.mult(60));                    }                }                if (FastMath.sqrt(sqr(ball.getLocalTranslation().x) + sqr(                        ball.getLocalTranslation().z)) < 3 && ball.getLocalTranslation().y > 19) {                    ball.getControl(RigidBodyControl.class).applyCentralForce(Vector3f.UNIT_X.mult(60));                }            });        }    }}