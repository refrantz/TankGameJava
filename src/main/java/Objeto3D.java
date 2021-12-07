

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.joml.Vector3f;

import logic.Ponto;

public class Objeto3D {
    ArrayList<ArrayList<Ponto>> faces; // vetor de faces
    int nFaces; // Variavel que armazena o numero de faces do objeto
    ArrayList<Ponto> normais;

    public Objeto3D()
    {
        this.faces = new ArrayList<ArrayList<Ponto>>();
        this.normais = new ArrayList<Ponto>();
        nFaces = 0;
    }

    public int getNFaces()
    {
        return nFaces;
    }
    public ArrayList<ArrayList<Ponto>> getFaces()
    {
        return faces;
    }

    public void LeObjeto (String nome)
    {
        File arquivo = new File(nome);
        Scanner scan = null;
        try {
            scan = new Scanner(arquivo);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        scan.nextLine();
        nFaces = Integer.parseInt(scan.nextLine());
        scan.nextLine();

        for(int i = 0; i < nFaces; i++){
            ArrayList<Ponto> face = new ArrayList<Ponto>();
            Ponto p1 = new Ponto(scan.nextFloat(),scan.nextFloat(),scan.nextFloat());
            Ponto p2 = new Ponto(scan.nextFloat(),scan.nextFloat(),scan.nextFloat());
            Ponto p3 = new Ponto(scan.nextFloat(),scan.nextFloat(),scan.nextFloat());
            face.add(p1);
            face.add(p2);
            face.add(p3);
            normais.add(calculaNormal(p1, p2, p3));
            scan.next();
            faces.add(face);
        }

    } 

    public static Ponto prodVetorial (Ponto v1, Ponto v2)
    {
        Ponto vresult = new Ponto(v1.y * v2.z - (v1.z * v2.y),v1.z * v2.x - (v1.x * v2.z),v1.x * v2.y - (v1.y * v2.x));

        return vresult;
    }

    public static Ponto vetUnitario(Ponto vet)
    {
        double modulo;

        modulo = Math.sqrt(vet.x * vet.x + vet.y * vet.y + vet.z * vet.z);

        vet.x /= modulo;
        vet.y /= modulo;
        vet.z /= modulo;

        return vet;
    }

    public static Ponto calculaNormal(Ponto p1, Ponto p2, Ponto p3){

        Ponto x = new Ponto(p2.x - p1.x, p2.y-p1.y, p2.z-p1.z);
        Ponto y = new Ponto(p3.x - p1.x, p3.y-p1.y, p3.z-p1.z);

        return vetUnitario(prodVetorial(x, y));

    }

    public Ponto getNormal(int index){

        return normais.get(index);

    }


}
