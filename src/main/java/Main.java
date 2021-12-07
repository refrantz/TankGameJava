

import glfw.listener.KeyListener;
import logic.Animate;
//import logic.Ponto;
import logic.Ponto;

import org.joml.Vector3f;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.Objects;

import java.awt.image.BufferedImage;

import static geometry.configuration.World.setCoordinatePlane;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;


import static de.damios.guacamole.gdx.StartOnFirstThreadHelper.startNewJvmIfRequired;

public class Main {
    private int width;
    private int height;
    private long glfwWindowAddress;
    private static Main INSTANCE = null;

    private static final int DEFAULT_WIDTH = 650;
    private static final int DEFAULT_HEIGHT = 500;
    private static final int MIN_WIDTH = 650;
    private static final int MIN_HEIGHT = 500;
    private static final int MAX_WIDTH = GL_DONT_CARE;
    private static final int MAX_HEIGHT = GL_DONT_CARE;
    private static final String TITLE = "Computacao Grafica - Trabalho 3";
    
    private Animate animate;
    private int ModoDeExibicao = 1;
    private int ModoDeProjecao = 1;
    private double AspectRatio;
    private float angulo = 90;
    private Vector3f rotacaoAmbiente = new Vector3f(20f, 0f, 0f);
    private boolean isRuinning = true;

    Ponto posJog = new Ponto(0,-4,0);

    private Long tempoDec = (long) 0;
    private Long tempoAnt = (long) 0;

    private float velocidade = (float) 0.025;

    private boolean andandoDireita = false;
    private boolean andandoFrente = false;
    private boolean andandoEsquerda = false;
    private boolean andandoTras = false;
    private boolean rotacionandoAnti = false;
    private boolean rotacionandoHora = false;

    private Vector3f frente = new Vector3f(-velocidade,0,0);
    private Vector3f tiro = new Vector3f(0,1,0);
    private Vector3f direita = new Vector3f(0,0,-velocidade);

    private Vector3f projetilVec = new Vector3f(0,1,0);
    private Ponto posProjetil = new Ponto(0,0,0);

    private int idGrama = 0;
    private int idTijolo = 0;
    private int idMetal = 0;

    private float maxText = 1f;

    private float posYText = 0;
    private float posXText = 0;
    private float posY2Text = maxText;
    private float posX2Text = maxText;

    private float anguloC1 = 0;
    private float anguloC2 = 0;

    private boolean rotacionandoC1AntiHora = false;
    private boolean rotacionandoC1Hora = false;
    private boolean rotacionandoC2AntiHora = false;
    private boolean rotacionandoC2Hora = false;

    private long tempoProjetil = 0;
    private long tempoProjetilAnt = 0;

    private boolean[][] pisoMorte = new boolean[50][25];
    private boolean[][] paredeMorte = new boolean[16][25];

    private float velocidadeProjetil = 15;
    private float velProjeTempo = 15;

    private Ponto pontoProjetil = new Ponto(0,0,0);

    private boolean colisao = false;

    private long cooldown = 2500;
    
    private int numObj = 15;

    private ArrayList<Objeto3D> listaObjs = new ArrayList<Objeto3D>();

    private int[] rand = new int[numObj];

    private Ponto[] randPontos = new Ponto[numObj];

    private int pontTotal = 0;

    private Main() {
        this.width = DEFAULT_WIDTH;
        this.height = DEFAULT_HEIGHT;
    }

    public static Main getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Main();
        }
        return INSTANCE;
    }

    public void run() {
        init();
        execution();
        terminateGracefully();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        boolean glfwStarted = glfwInit();
        
        // Inicia Animate
        animate = new Animate();

        // Throw error and terminate if GLFW initialization fails
        if (!glfwStarted) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowAddress = createAndConfigureWindow();

        // Make the OpenGL context current
        glfwMakeContextCurrent(glfwWindowAddress);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        setCoordinatePlane();
        setListeners();
        // Make the window visible
        glfwShowWindow(glfwWindowAddress);

        //WindowResizeListener.getInstance().invoke(glfwWindowAddress, width, height);
        reshape(width, height);

        initGL();
        glEnable(GL_TEXTURE_2D);
        
        BufferedImage grama = TextureLoader.loadImage("imgs/grama.png");
        BufferedImage tijolo = TextureLoader.loadImage("imgs/tijolo.png");
        BufferedImage metal = TextureLoader.loadImage("imgs/metal.jpg");
        
        idGrama = TextureLoader.loadTexture(grama);
        idTijolo = TextureLoader.loadTexture(tijolo);
        idMetal = TextureLoader.loadTexture(metal);

        for(boolean[] x : pisoMorte){
            for(boolean b : x){
                b = true;
            }
        }

        for(boolean[] x : paredeMorte){
            for(boolean b : x){
                b = true;
            }
        }

        Objeto3D obj1 = new Objeto3D();
        obj1.LeObjeto("D:/PUCRS/4_SEMESTRE/Fundamentos de Computacao Grafica/ExemploBasico3D_Java/src/main/java/objs/caminhao.tri");

        Objeto3D obj2 = new Objeto3D();
        obj2.LeObjeto("D:/PUCRS/4_SEMESTRE/Fundamentos de Computacao Grafica/ExemploBasico3D_Java/src/main/java/objs/tree.tri");

        Objeto3D obj3 = new Objeto3D();
        obj3.LeObjeto("D:/PUCRS/4_SEMESTRE/Fundamentos de Computacao Grafica/ExemploBasico3D_Java/src/main/java/objs/tree.tri");

        listaObjs.add(obj1);
        listaObjs.add(obj2);
        listaObjs.add(obj3);

        for(int i = 0; i < rand.length; i++){
            rand[i] = (int) Math.round(Math.random()*2);
        }

        for(int i = 0; i < randPontos.length/2; i++){
            randPontos[i] = new Ponto((float) (Math.random()*25)-20, 0, (float) (-Math.random()*25)+5);
        }
        for(int i = randPontos.length/2; i < randPontos.length; i++){
            randPontos[i] = new Ponto((float) (Math.random()*25), 0, (float) (-Math.random()*25)+5);
        }
    }

    void initGL(){
        glClearColor(0.0f, 0.0f, 1.0f, 1.0f); // Fundo de tela preto
   
        glShadeModel(GL_SMOOTH);
        //glShadeModel(GL_FLAT);
        glColorMaterial ( GL_FRONT, GL_AMBIENT_AND_DIFFUSE );
        if (ModoDeExibicao == 1) // Faces Preenchidas??
        {
            glEnable(GL_DEPTH_TEST);
            glEnable (GL_CULL_FACE );
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
        else
        {
            glEnable(GL_DEPTH_TEST);
            glEnable (GL_CULL_FACE );
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
        glEnable(GL_NORMALIZE);
    }

    /**
     * Executa o loop principal.
     */
    private void execution() {
        // This is the main loop
        while (!glfwWindowShouldClose(glfwWindowAddress) && isRuinning) {
            animate.startCounter();

            keyListenerExample();
            display();
            glfwPollEvents();

            animate.stopAndShow();
        }
    }

    /**
    * This is an example on how to use the implemented {@link KeyListener}
    * In this example, color is set from red to blue while the SPACE key is pressed on the keyboard
    * */
    private void keyListenerExample() {
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_W)) {
            andandoFrente = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_S)) {
            andandoTras = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_A)) {
            andandoEsquerda = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_D)) {
            andandoDireita = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_E)) {
            rotacionandoHora = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_Q)) {
            rotacionandoAnti = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_O)) {
            rotacionandoC1Hora = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_P)) {
            rotacionandoC1AntiHora = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_L)) {
            rotacionandoC2Hora = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_K)) {
            rotacionandoC2AntiHora = true;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_Z)) {
            if(tempoProjetil > cooldown){
                tempoProjetil = 0;
                colisao = false;
            }
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_M)) {
            velocidadeProjetil += 0.1;
        }
        if (KeyListener.getInstance().isKeyPressed(GLFW_KEY_N)) {
            if(velocidadeProjetil > 0){
                velocidadeProjetil -= 0.1;
            }
        }

    }

    /**
     * Desenha o conteudo do frame a cada iteracao do loop principal.
     */
    private void display() {
        

        glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

        //DefineLuz();

        PosicUser();

        glDisable(GL_TEXTURE_2D);
            geraObjetos(numObj);
        glEnable(GL_TEXTURE_2D);
        
        glMatrixMode(GL_MODELVIEW);

        glBindTexture(GL_TEXTURE_2D, idGrama);
        DesenhaPiso();
        
        glBindTexture(GL_TEXTURE_2D, idTijolo);
        DesenhaParedao();

        glPushMatrix();
            glTranslated(-posJog.x, 1, -posJog.z);
            glRotatef(90-angulo, 0, 1, 0);
            glTranslated(-1, 0, 1);
                
            glColor3f(0.6156862745f, 0.8980392157f, 0.9803921569f); // Azul claro
            glDisable(GL_TEXTURE_2D);
            DesenhaJogador();
            glEnable(GL_TEXTURE_2D);


            glTranslated(1, 0.5, -1);
            glRotated(90, 0, 0, 1);
            glRotated(90, 0, 1, 0);
            glRotated(anguloC1, 0, 0, 1);
            glColor3f(5, 0.8980392157f, 0.9803921569f);


            glBindTexture(GL_TEXTURE_2D, idMetal);
            desenhaCilindro(0.5, 1);
            glTranslated(0, 0, 1);
            glRotated(anguloC2, 1, 0, 0);
            glColor3f(0.6156862745f, 5, 0.9803921569f); 
            desenhaCilindro(0.3, 0.8);

            glColor3f(8, 5, 5); 
            glTranslated(0, 0, 1);

        glPopMatrix();

        glPushMatrix();
            glDisable(GL_TEXTURE_2D);

            if(tempoProjetil > 0 && tempoProjetil < cooldown && colisao == false){
                desenhaProjetil(posProjetil, tempoProjetil, projetilVec, velProjeTempo);
            }else{
                posProjetil.set(posJog.x, 3, posJog.z); 
                projetilVec.set(tiro);
                velProjeTempo = velocidadeProjetil;
            }

            glEnable(GL_TEXTURE_2D);
        glPopMatrix();
        

        tempoProjetil += System.currentTimeMillis() - tempoProjetilAnt;
        tempoProjetilAnt = System.currentTimeMillis();
        
        glfwSwapBuffers(glfwWindowAddress);
    }

    private void rotacionaAmbiente(){
        glRotated(rotacaoAmbiente.x, 1, 0, 0);
        glRotated(rotacaoAmbiente.y, 0, 1, 0);
        glRotated(rotacaoAmbiente.z, 0, 0, 1);
    }

    // **********************************************************************
    //  void DefineLuz(void)
    // **********************************************************************
    void DefineLuz()
    {
        // Define cores para um objeto dourado
        float LuzAmbiente[]   = {0.4f, 0.4f, 0.4f};
        float LuzDifusa[]   = {0.7f, 0.7f, 0.7f};
        float LuzEspecular[] = {0.9f, 0.9f, 0.9f};
        float PosicaoLuz0[]  = {0.0f, 3.0f, 5.0f};  // Posi��o da Luz
        float Especularidade[] = {1.0f, 1.0f, 1.0f};

        // ****************  Fonte de Luz 0

        glEnable ( GL_COLOR_MATERIAL );

        // Habilita o uso de ilumina��o
        glEnable(GL_LIGHTING);

        // Ativa o uso da luz ambiente
        glLightModelfv(GL_LIGHT_MODEL_AMBIENT, LuzAmbiente);
        // Define os parametros da luz n�mero Zero
        glLightfv(GL_LIGHT0, GL_AMBIENT, LuzAmbiente);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, LuzDifusa  );
        glLightfv(GL_LIGHT0, GL_SPECULAR, LuzEspecular  );
        glLightfv(GL_LIGHT0, GL_POSITION, PosicaoLuz0 );
        glEnable(GL_LIGHT0);

        // Ativa o "Color Tracking"
        glEnable(GL_COLOR_MATERIAL);

        // Define a reflectancia do material
        glMaterialfv(GL_FRONT,GL_SPECULAR, Especularidade);

        // Define a concentra��oo do brilho.
        // Quanto maior o valor do Segundo parametro, mais
        // concentrado ser� o brilho. (Valores v�lidos: de 0 a 128)
        glMateriali(GL_FRONT,GL_SHININESS,51);
    }

    /**
     * Cria e configura a instancia da janela grafica.
     * 
     * @return endereco da janela
     */
    private long createAndConfigureWindow() {
        // Create the window
        long windowAddress = glfwCreateWindow(this.width, this.height, TITLE, NULL, NULL);

        if (windowAddress == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // glfw window Configuration
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwSetWindowSizeLimits(windowAddress, MIN_WIDTH, MIN_HEIGHT, MAX_WIDTH, MAX_HEIGHT);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        return windowAddress;
    }

    // **********************************************************************
    //  void reshape( int w, int h )
    //		trata o redimensionamento da janela OpenGL
    //
    // **********************************************************************
    void reshape( int w, int h )
    {

        // Evita divis�o por zero, no caso de uam janela com largura 0.
        if(h == 0) h = 1;
        // Ajusta a rela��o entre largura e altura para evitar distor��o na imagem.
        // Veja fun��o "PosicUser".
        AspectRatio = 1.0f * w / h;
        // Reset the coordinate system before modifying
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // Seta a viewport para ocupar toda a janela
        glViewport(0, 0, w, h);
        //cout << "Largura" << w << endl;

        width = w;
        height = h;
    }

    /**
     * Define as callbacks da janela.
     */
    private void setListeners() {
        glfwSetKeyCallback(glfwWindowAddress, KeyListener.getInstance());
        //glfwSetWindowSizeCallback(glfwWindowAddress, WindowResizeListener.getInstance());
        glfwSetWindowSizeCallback(glfwWindowAddress, (long window, int width, int height) -> {
            reshape(width, height);
        });
    }

    /**
     * Finaliza o programa, liberando as memorias ocupadas.
     */
    private void terminateGracefully() {
        // Free memory upon leaving
        glfwFreeCallbacks(glfwWindowAddress);
        glfwDestroyWindow(glfwWindowAddress);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    // **********************************************************************
    //  void PosicUser()
    // **********************************************************************
    void PosicUser()
    {

        // Define os par�metros da proje��o Perspectiva
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // Define o volume de visualiza��o sempre a partir da posicao do
        // observador
        if (ModoDeProjecao == 0){
            glOrtho(-10, 10, -10, 10, 0, 50); // Projecao paralela Orthografica
        }else {
            perspective(90,AspectRatio,0.01,50);
        }
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        Vector3f mulFrente = frente.mul(tempoDec);

        if(andandoFrente){
            if ((posJog.somaResult(mulFrente).z > -5 && posJog.somaResult(mulFrente).z < 20) && (posJog.somaResult(mulFrente).x > -30 && posJog.somaResult(mulFrente).x < 20) && ((posJog.somaResult(mulFrente).x > -4 || posJog.somaResult(mulFrente).x < -7) || (paredeMorte[1][(int) -posJog.z+20] && paredeMorte[2][(int) - posJog.z+20]))){
                posJog.somaVetor(mulFrente);
            }
        }else if(andandoTras){
            if ((posJog.subtraiResult(mulFrente).z > -5 && posJog.subtraiResult(mulFrente).z < 20) && (posJog.subtraiResult(mulFrente).x > -30 && posJog.subtraiResult(mulFrente).x < 20) && ((posJog.subtraiResult(mulFrente).x > -4 || posJog.subtraiResult(mulFrente).x < -7) || (paredeMorte[1][(int) -posJog.z+20] && paredeMorte[2][(int) - posJog.z+20]))){
                posJog.subtraiVetor(mulFrente);
            }
        }

        Vector3f mulDireita = direita.mul(tempoDec);

        if(andandoDireita){
            if ((posJog.somaResult(mulDireita).z > -5 && posJog.somaResult(mulDireita).z < 20) && (posJog.somaResult(mulDireita).x > -30 && posJog.somaResult(mulDireita).x < 20) && ((posJog.somaResult(mulDireita).x > -4 || posJog.somaResult(mulDireita).x < -7) || (paredeMorte[1][(int) -posJog.z+20] && paredeMorte[2][(int) - posJog.z+20]))){
                posJog.somaVetor(mulDireita);
            }
        }else if(andandoEsquerda){
            if ((posJog.subtraiResult(mulDireita).z > -5 && posJog.subtraiResult(mulDireita).z < 20) && (posJog.subtraiResult(mulDireita).x > -30 && posJog.subtraiResult(mulDireita).x < 20) && ((posJog.subtraiResult(mulDireita).x > -4 || posJog.subtraiResult(mulDireita).x < -7) || (paredeMorte[1][(int) -posJog.z+20] && paredeMorte[2][(int) - posJog.z+20]))){
                posJog.subtraiVetor(mulDireita);
            }
        }


        frente.set(-velocidade, 0, 0);
        direita.set(0, 0, -velocidade);

        frente.rotateAxis((float) Math.toRadians(90-angulo), 0.0f, 1.0f, 0.0f);
        direita.rotateAxis((float) Math.toRadians(90-angulo), 0.0f, 1.0f, 0.0f);

        if(rotacionandoHora){
            angulo += velocidade*tempoDec*2;
        }else if(rotacionandoAnti){
            angulo -= velocidade*tempoDec*2;
        }

        if(rotacionandoC1Hora){
            anguloC1 += velocidade*tempoDec*2;
        }else if(rotacionandoC1AntiHora){
            anguloC1 -= velocidade*tempoDec*2;
        }

        if(rotacionandoC2Hora){
            anguloC2 += velocidade*tempoDec*2;
        }else if(rotacionandoC2AntiHora){
            anguloC2 -= velocidade*tempoDec*2;
        }

        andandoDireita = false;
        andandoEsquerda = false;
        andandoFrente = false;
        andandoTras = false;
        rotacionandoAnti = false;
        rotacionandoHora = false;
        rotacionandoC1Hora = false;
        rotacionandoC1AntiHora = false;
        rotacionandoC2Hora = false;
        rotacionandoC2AntiHora = false;

        tiro.set(0, 1, 0);
       
        tiro.rotateAxis((float) Math.toRadians(-anguloC2), 0.0f, 0.0f, 1.0f);
        tiro.rotateAxis((float) Math.toRadians(anguloC1), 0.0f, 1.0f, 0.0f);
        tiro.rotateAxis((float) Math.toRadians(90-angulo), 0.0f, 1.0f, 0.0f);

        glTranslated(0, 0, -7);
        glRotated(angulo, 0, 1, 0);
        glTranslated(posJog.x, posJog.y, posJog.z);

        tempoDec = (System.currentTimeMillis() - tempoAnt);
        tempoAnt = System.currentTimeMillis();
        
        //rotacionaAmbiente();
    }

    void perspective(double fieldOfView, double aspectRatio, double zNear, double zFar){
        double fH = Math.tan( (fieldOfView / 360.0f * 3.14159f) ) * zNear;
        double fW = fH * aspectRatio;
        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }

    // Desenhos
    // **********************************************************************
    //  void DesenhaCubo()
    // **********************************************************************
    void DesenhaCubo(float tamAresta)
    {
        glBegin ( GL_QUADS );
        // Front Face
        glNormal3f(0,0,1);
        glVertex3f(-tamAresta/2, -tamAresta/2,  tamAresta/2);
        glVertex3f( tamAresta/2, -tamAresta/2,  tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2,  tamAresta/2);
        glVertex3f(-tamAresta/2,  tamAresta/2,  tamAresta/2);
        // Back Face
        glNormal3f(0,0,-1);
        glVertex3f(-tamAresta/2, -tamAresta/2, -tamAresta/2);
        glVertex3f(-tamAresta/2,  tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2, -tamAresta/2, -tamAresta/2);
        // Top Face
        glNormal3f(0,1,0);
        glVertex3f(-tamAresta/2,  tamAresta/2, -tamAresta/2);
        glVertex3f(-tamAresta/2,  tamAresta/2,  tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2,  tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2, -tamAresta/2);
        // Bottom Face
        glNormal3f(0,-1,0);
        glVertex3f(-tamAresta/2, -tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2, -tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2, -tamAresta/2,  tamAresta/2);
        glVertex3f(-tamAresta/2, -tamAresta/2,  tamAresta/2);
        // Right face
        glNormal3f(1,0,0);
        glVertex3f( tamAresta/2, -tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2, -tamAresta/2);
        glVertex3f( tamAresta/2,  tamAresta/2,  tamAresta/2);
        glVertex3f( tamAresta/2, -tamAresta/2,  tamAresta/2);
        // Left Face
        glNormal3f(-1,0,0);
        glVertex3f(-tamAresta/2, -tamAresta/2, -tamAresta/2);
        glVertex3f(-tamAresta/2, -tamAresta/2,  tamAresta/2);
        glVertex3f(-tamAresta/2,  tamAresta/2,  tamAresta/2);
        glVertex3f(-tamAresta/2,  tamAresta/2, -tamAresta/2);
        glEnd();

    }

    void DesenhaJogador()
    {
        glPushMatrix();
            glTranslatef(0,0,-1);
            glScalef(3,1,2);
            DesenhaCubo(1);
        glPopMatrix();
    }

    void DesenhaParalelepipedo()
    {
        glPushMatrix();
            glTranslatef(0,0,-1);
            glScalef(1,1,2);
            DesenhaCubo(1);
        glPopMatrix();
    }

    // **********************************************************************
    // void DesenhaLadrilho(int corBorda, int corDentro)
    // Desenha uma c�lula do piso.
    // Eh possivel definir a cor da borda e do interior do piso
    // O ladrilho tem largula 1, centro no (0,0,0) e est� sobre o plano XZ
    // **********************************************************************
    void DesenhaLadrilho(int corBorda, int corDentro)
    {
        //defineCor(corDentro);// desenha QUAD preenchido
        glColor3f(1,1,1);  

        glBegin ( GL_QUADS );
            glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(-0.5f,  0.0f, -0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(-0.5f,  0.0f,  0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( 0.5f,  0.0f,  0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( 0.5f,  0.0f, -0.5f);
        glEnd();

        glBegin ( GL_QUADS );
            glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(-0.5f,  -1, -0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(-0.5f,  -1,  0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( 0.5f,  -1,  0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( 0.5f,  -1, -0.5f);
        glEnd();


        glBegin ( GL_QUADS );
            glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(-0.5f,  -1, 0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(-0.5f,  0,  0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( 0.5f,  0,  0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( 0.5f,  -1, 0.5f);
        glEnd();

        glBegin ( GL_QUADS );
            glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(-0.5f,  -1, -0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(-0.5f,  0,  -0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( 0.5f,  0,  -0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( 0.5f,  -1, -0.5f);
        glEnd();

        glBegin ( GL_QUADS );
        glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(0.5f,  0, -0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(0.5f,  0,  0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( 0.5f,  -1,  0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( 0.5f,  -1, -0.5f);
        glEnd();

        glBegin ( GL_QUADS );
        glNormal3f(0,1,0);
            glTexCoord2f(posXText, posY2Text);
            glVertex3f(-0.5f,  0, -0.5f);
            glTexCoord2f(posX2Text, posY2Text);
            glVertex3f(-0.5f,  0,  0.5f);
            glTexCoord2f(posX2Text, posYText);
            glVertex3f( -0.5f,  -1,  0.5f);
            glTexCoord2f(posXText, posYText);
            glVertex3f( -0.5f,  -1, -0.5f);
        glEnd();

    }

    // **********************************************************************
    //
    //
    // **********************************************************************
    void DesenhaPiso()
    {

        posXText = 0;
        posYText = 0;
        posX2Text = maxText;
        posY2Text = maxText;
        glPushMatrix();
        glTranslated(-20, 0, -20);
        for(int x=0; x<50;x++)
        {
            glPushMatrix();
            for(int z=0; z<25;z++)
            { 
                if(pisoMorte[x][z] != true && ((pontoProjetil.x >= x-20 && pontoProjetil.x <=x-18) && (pontoProjetil.y <= -1 && pontoProjetil.y >= -2) && (pontoProjetil.z >= z-20 && pontoProjetil.z <= z-18))){
                    pontTotal -= 5;
                    System.out.println("Pontuacao: " + pontTotal);
                    pisoMorte[x][z] = true;
                    colisao = true;
                }else if(!pisoMorte[x][z]){
                    DesenhaLadrilho(0, 0);
                }
                glTranslated(0, 0, 1);
            }
            glPopMatrix();
            glTranslated(1, 0, 0);
        }
        glPopMatrix();
    }

    void DesenhaParedao()
    {

        posYText = maxText;
        posY2Text = maxText + maxText/16;

        glPushMatrix();

            glTranslated(5, -1, -20);
            glRotated(90, 0, 0, 1);

            for(int x=0; x<=15;x++)
            {
                posYText -= maxText/16;
                posY2Text -= maxText/16;

                glPushMatrix();
                    for(int z=0; z<25;z++)
                    { 
                        posXText += maxText/26;
                        posX2Text += maxText/26;
                        if(paredeMorte[x][z] != true && ((pontoProjetil.x >= 3.5 && pontoProjetil.x <= 4.5)  && (pontoProjetil.y >= x-3 && pontoProjetil.y <= x-1) && (pontoProjetil.z >= z-21 && pontoProjetil.z <= z-19))){
                            pontTotal += 5;
                            System.out.println("Pontuacao: " + pontTotal);
                            paredeMorte[x][z] = true;
                            colisao = true;
                        }else if(!paredeMorte[x][z]){
                            DesenhaLadrilho(0, 0);
                        }
                        glTranslated(0, 0, 1);
                    }
                glPopMatrix();
                glTranslated(1, 0, 0);
                posXText = 0;
                posX2Text = maxText/26;
            }

        glPopMatrix();
    }

    void desenhaCilindro(double radius, double length){
        int steps = 100;
        double varAng = Math.PI * 2/steps;
        float ang = 0;
        double prevX = 0;
        double prevY = 0;
        double x = 0;
        double y = 0;
        for(int i = 0; i<=steps; i++){
            ang += varAng;
            x = radius * Math.sin(ang);
            y = radius * Math.cos(ang);

            glBegin(GL_TRIANGLES);
                glTexCoord2d(0, 0);
                glVertex3d(0, 0, 0);
                glTexCoord2d(1, 0);
                glVertex3d(prevX, prevY, 0);
                glTexCoord2d(1, 1);
                glVertex3d(x, y, 0);
            glEnd();

            glDisable(GL_CULL_FACE);

            glBegin(GL_TRIANGLES);
                glTexCoord2d(0, 0);
                glVertex3d(0, 0, length);
                glTexCoord2d(1, 0);
                glVertex3d(prevX, prevY, length);
                glTexCoord2d(1, 1);
                glVertex3d(x, y, length);
            glEnd();

            glBegin(GL_QUAD_STRIP);
                glTexCoord2d(0, 0);
                glVertex3d(prevX, prevY, 0);
                glTexCoord2d(0, 1);
                glVertex3d(x, y, 0);
                glTexCoord2d(1, 1);
                glVertex3d(prevX, prevY, length);
                glTexCoord2d(1, 0);
                glVertex3d(x, y, length);
            glEnd();

            prevX = x;
            prevY = y;
        }

    }

    static void desenhaEsfera(double r, int sizeX, int sizeY) {
        int i, j;
        for(i = 0; i <= sizeX; i++) {
            double lat0 = Math.PI * (-0.5 + (double) (i - 1) / sizeX);
            double z0  = Math.sin(lat0);
            double zr0 =  Math.cos(lat0);
    
            double lat1 = Math.PI * (-0.5 + (double) i / sizeX);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);
    
            glBegin(GL_QUAD_STRIP);
            for(j = 0; j <= sizeY; j++) {
                double lng = 2 * Math.PI * (double) (j - 1) / sizeY;
                double x = Math.cos(lng);
                double y = Math.sin(lng);
    
                glNormal3d(x * zr0, y * zr0, z0);
                glVertex3d(r * x * zr0, r * y * zr0, r * z0);
                glNormal3d(x * zr1, y * zr1, z1);
                glVertex3d(r * x * zr1, r * y * zr1, r * z1);
            }
            glEnd();
        }
    }

    void desenhaProjetil(Ponto inicio, Long t, Vector3f vetor, float velocidade){
        glTranslated(-inicio.x, inicio.y, -inicio.z);
        double vx = velocidade * vetor.x;
        double vy = velocidade * vetor.y;
        double vz = velocidade * vetor.z;
        double ts = t;
        ts = ts/1000;

        double x = 0;
        double y = 0;
        double z = 0;

        float g = 9.81f;

        x = vx * ts;
        y = vy * ts - 0.5f * g * ts * ts;
        z = vz * ts;

        pontoProjetil.set((float) x-inicio.x, (float) y+2, (float) z-inicio.z);
        
        glPushMatrix();
            glTranslated(x, y, z);
            desenhaEsfera(0.25, 20, 20);
        glPopMatrix();

    }

    void exibeObjeto(int indexObj){
        for (int i = 0; i<listaObjs.get(indexObj).getFaces().size(); i++){
            glBegin(GL_TRIANGLES);
                glColor3f(0.1f, 0.1f, 0.1f);
                ArrayList<Ponto> face = listaObjs.get(indexObj).getFaces().get(i);
                glNormal3f(listaObjs.get(indexObj).getNormal(i).x, listaObjs.get(indexObj).getNormal(i).y, listaObjs.get(indexObj).getNormal(i).z);
                glVertex3f(face.get(0).x, face.get(0).y, face.get(0).z);
                glVertex3f(face.get(1).x, face.get(1).y, face.get(1).z);
                glVertex3f(face.get(2).x, face.get(2).y, face.get(2).z);
            glEnd();
        }
    }
    
    void geraObjetos(int numObjs){
        for(int i = 0; i < numObjs; i++){
            glPushMatrix();
                glTranslatef(randPontos[i].x, randPontos[i].y , randPontos[i].z);
                exibeObjeto(rand[i]);
            glPopMatrix();
        }
    }

    public static void main(String[] args) {
        // Starts a new JVM if the application was started on macOS without the
        // -XstartOnFirstThread argument.
        if (startNewJvmIfRequired()) {
            System.exit(0);
        }

        Main.getInstance().run();
    }
}
