package pl.rembol.jme3.tjug;

import com.jme3.app.Application;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;

public class Ball extends Node {

    private static final int TEX_SIZE = 128;

    public Ball(Application application, Node rootNode, Vector3f position, String text) {
        Sphere sphere = new Sphere(6, 12, 1f);

        Texture2D texture = prepareTexture(application, text);

        Material material = new Material(application.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        material.setTexture("DiffuseMap", texture);
        material.setColor("Ambient", ColorRGBA.White);
        material.setColor("Specular", ColorRGBA.White);
        Geometry geometry = new Geometry("ball", sphere);
        geometry.setMaterial(material);

        attachChild(geometry);
        setLocalTranslation(position);
        rootNode.attachChild(this);

        RigidBodyControl rigidBodyControl = new RigidBodyControl(new SphereCollisionShape(1f), 1f);
        addControl(rigidBodyControl);
        rigidBodyControl.setKinematic(true);
        application.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(rigidBodyControl);
        setShadowMode(RenderQueue.ShadowMode.Receive);
        setUserData("number", text);
    }

    private Texture2D prepareTexture(Application application, String text) {
        Geometry geometry = new Geometry("slide", new Quad(1, 1));
        geometry.setLocalTranslation(-.5f, -.5f, -1);
        Material material = new Material(application.getAssetManager(),
                "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        material.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        geometry.setQueueBucket(RenderQueue.Bucket.Transparent);
        geometry.setMaterial(material);

        BitmapText bitmapText = new BitmapText(
                application.getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
        bitmapText.setColor(ColorRGBA.Black);
        bitmapText.setText(text);
        bitmapText.setQueueBucket(RenderQueue.Bucket.Transparent);
        bitmapText.setLocalScale(0.01f);
        bitmapText.setLocalTranslation(-bitmapText.getLineWidth() * 0.01f / 2 - .25f, bitmapText.getLineHeight() * 0.01f / 2, 0f);

        BitmapText bitmapText2 = new BitmapText(
                application.getAssetManager().loadFont("Interface/Fonts/Default.fnt"));
        bitmapText2.setColor(ColorRGBA.Black);
        bitmapText2.setText(text);
        bitmapText2.setQueueBucket(RenderQueue.Bucket.Transparent);
        bitmapText2.setLocalScale(0.01f);
        bitmapText2.setLocalTranslation(-bitmapText2.getLineWidth() * 0.01f / 2 + .25f, bitmapText2.getLineHeight() * 0.01f / 2, 0f);

        Camera camera = new Camera(TEX_SIZE, TEX_SIZE);
        camera.setFrustumFar(3);
        camera.setLocation(new Vector3f(0, 0, 1));
        camera.lookAt(Vector3f.ZERO, Vector3f.UNIT_Y);
        ViewPort modelView = application.getRenderManager().createPreView("modelView", camera);
        modelView.setClearFlags(true, true, true);
        FrameBuffer offBuffer = new FrameBuffer(TEX_SIZE, TEX_SIZE, 1);
        offBuffer.setDepthBuffer(Image.Format.Depth);
        Texture2D texture = new Texture2D(TEX_SIZE, TEX_SIZE, Image.Format.RGBA8);
        offBuffer.setColorTexture(texture);

        modelView.setOutputFrameBuffer(offBuffer);
        Node scene = new Node("scene");
        scene.attachChild(geometry);
        scene.attachChild(bitmapText);
        scene.attachChild(bitmapText2);

        scene.updateGeometricState();
        modelView.attachScene(scene);

        return texture;
    }
}
