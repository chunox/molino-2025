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

    /**
     * Agrega un objeto al archivo (append)
     * @param obj Objeto a serializar
     * @return true si fue exitoso, false en caso contrario
     */
    public boolean addOneObject(Object obj) {
        try {
            File file = new File(fileName);
            ObjectOutputStream oos;

            if (file.exists() && file.length() > 0) {
                oos = new AddableObjectOutputStream(
                    new FileOutputStream(fileName, true)
                );
            } else {
                oos = new ObjectOutputStream(
                    new FileOutputStream(fileName, true)
                );
            }

            oos.writeObject(obj);
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lee múltiples objetos del archivo
     * @return Array de objetos deserializados o null si está vacío
     */
    public Object[] readObjects() {
        ArrayList<Object> list = new ArrayList<>();
        try {
            File file = new File(fileName);
            if (!file.exists() || file.length() == 0) {
                return null;
            }

            ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName)
            );

            while (true) {
                try {
                    Object obj = ois.readObject();
                    list.add(obj);
                } catch (EOFException e) {
                    break;
                }
            }
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return list.isEmpty() ? null : list.toArray();
    }

    /**
     * Clase auxiliar para permitir append a ObjectOutputStream
     */
    private static class AddableObjectOutputStream extends ObjectOutputStream {
        public AddableObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }
    }
}
