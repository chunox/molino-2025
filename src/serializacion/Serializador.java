package serializacion;

import java.io.*;
import java.util.ArrayList;

/**
 * Clase genérica para serializar y deserializar objetos a archivos
 */
public class Serializador {
    private String fileName;

    public Serializador(String fileName) {
        this.fileName = fileName;
        crearDirectorioSiNoExiste();
    }

    private void crearDirectorioSiNoExiste() {
        File file = new File(fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }

    /**
     * Escribe un objeto al archivo (sobreescribe el contenido)
     * @param obj Objeto a serializar
     * @return true si fue exitoso, false en caso contrario
     */
    public boolean writeOneObject(Object obj) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(fileName)
            );
            oos.writeObject(obj);
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lee el primer objeto del archivo
     * @return El objeto deserializado o null si no existe o hay error
     */
    public Object readFirstObject() {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }

            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName)
            );
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
