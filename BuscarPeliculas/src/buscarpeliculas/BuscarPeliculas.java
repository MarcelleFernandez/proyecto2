/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package buscarpeliculas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Esta clase representa una aplicación para buscar y mostrar información sobre
 * películas utilizando la API de TMDb.
 */
public class BuscarPeliculas extends JFrame {

    private JTextField searchField;
    private JButton searchButton;
    private JPanel contentPane;

    /**
     * Constructor de la clase BuscarPeliculas. Inicializa la interfaz gráfica y
     * carga las películas populares al iniciar la aplicación.
     */
    public BuscarPeliculas() {
        setTitle("Movies from TMDb");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel superior con el campo de búsqueda y el botón
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());

        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchMovies();
            }
        });

        topPanel.add(new JLabel("Search: "));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // Panel principal que contendrá las imágenes de las películas
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayout(0, 4, 10, 10));

        // ScrollPane para permitir el desplazamiento si hay muchas películas
        JScrollPane scrollPane = new JScrollPane(contentPane);

        // Cargar las películas populares al inicio
        loadPopularMovies();

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    /**
     * Carga las películas populares utilizando la API de TMDb y muestra las
     * imágenes en la interfaz.
     */
    private void loadPopularMovies() {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/popular?api_key=579950b8c5c2bb8f4979af5b90103b04&language=es-ES&page=1");
            fetchAndDisplayMovies(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Realiza una búsqueda de películas utilizando el término ingresado por el
     * usuario y muestra los resultados.
     */
    private void searchMovies() {
        String searchTerm = searchField.getText();
        if (!searchTerm.isEmpty()) {
            try {
                URL searchUrl = new URL("https://api.themoviedb.org/3/search/movie?api_key=579950b8c5c2bb8f4979af5b90103b04&language=es-ES&query=" + searchTerm);
                fetchAndDisplayMovies(searchUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Realiza la conexión a la API de TMDb, obtiene la información de las
     * películas y muestra las imágenes en la interfaz.
     *
     * @param url La URL de la API para obtener información sobre películas.
     */
    private void fetchAndDisplayMovies(URL url) {
        contentPane.removeAll();

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            for (JsonElement element : results) {
                JsonObject movie = element.getAsJsonObject();

                // Obtener información de la película
                String title = getStringOrDefault(movie, "title", "No Title");
                String posterPath = getStringOrDefault(movie, "poster_path", "");
                String overview = getStringOrDefault(movie, "overview", "No Overview");
                String releaseDate = getStringOrDefault(movie, "release_date", "Release Date: N/A");

                int movieId = movie.getAsJsonPrimitive("id").getAsInt();

                // Cargar la imagen de la película y agregarla a la interfaz
                BufferedImage img;
                try {
                    img = loadImage(posterPath, 150, 225);
                    if (img != null) {
                        JLabel picLabel = new JLabel(new ImageIcon(img));
                        picLabel.setToolTipText(title);
                        picLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                showMovieDetails(movieId, title, posterPath, overview, releaseDate);
                            }
                        });
                        contentPane.add(picLabel);
                    } else {
                        System.err.println("Error al cargar la imagen para la película: " + title);
                    }
                } catch (IOException e) {
                    System.err.println("Error al cargar la imagen para la película: " + title);
                    img = new BufferedImage(150, 225, BufferedImage.TYPE_INT_RGB);
                }
            }

            revalidate();
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Muestra los detalles de una película, incluyendo una imagen más grande,
     * información y películas similares.
     *
     * @param movieId El ID de la película.
     * @param title El título de la película.
     * @param posterPath La ruta del póster de la película.
     * @param overview La descripción de la película.
     * @param releaseDate La fecha de lanzamiento de la película.
     */
    private void showMovieDetails(int movieId, String title, String posterPath, String overview, String releaseDate) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BorderLayout());

        try {
            // Cargar una imagen más grande de la película
            BufferedImage img = loadImage(posterPath, 400, 600);
            JLabel picLabel = new JLabel(new ImageIcon(img));
            detailsPanel.add(picLabel, BorderLayout.CENTER);

            // Mostrar detalles de la película, actores y películas similares
            JTextArea detailsTextArea = new JTextArea("Title: " + title + "\n\nOverview: " + overview + "\n\nRelease Date: " + releaseDate);
            detailsTextArea.setLineWrap(true);
            detailsTextArea.setWrapStyleWord(true);
            detailsTextArea.setEditable(false);
            addTextAreaWithScroll(detailsPanel, detailsTextArea, BorderLayout.NORTH);

            JTextArea actorsTextArea = new JTextArea("Actors: " + getMovieActors(movieId));
            addTextAreaWithScroll(detailsPanel, actorsTextArea, BorderLayout.WEST);

            String similarMovies = getSimilarMovies(movieId);
            JTextArea similarMoviesTextArea = new JTextArea("Similar Movies: " + similarMovies);
            addTextAreaWithScroll(detailsPanel, similarMoviesTextArea, BorderLayout.EAST);

            // Botón para cerrar la ventana de detalles
            JButton backButton = new JButton("Back");
            backButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(backButton);

            dialog.add(detailsPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Agrega un área de texto con desplazamiento a un contenedor en una
     * posición específica.
     *
     * @param container El contenedor al que se agregará el área de texto.
     * @param textArea El área de texto que se agregará.
     * @param position La posición en la que se agregará el área de texto en el
     * contenedor.
     */
    private void addTextAreaWithScroll(Container container, JTextArea textArea, String position) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        container.add(scrollPane, position);
    }

    /**
     * Obtiene los nombres de los actores de una película utilizando la API de
     * TMDb.
     *
     * @param movieId El ID de la película.
     * @return Una cadena con los nombres de los actores o un mensaje de error.
     */
    private String getMovieActors(int movieId) {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/" + movieId + "/credits?api_key=579950b8c5c2bb8f4979af5b90103b04");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray cast = jsonObject.getAsJsonArray("cast");

            StringBuilder actors = new StringBuilder();
            for (JsonElement element : cast) {
                JsonObject actor = element.getAsJsonObject();
                String actorName = actor.getAsJsonPrimitive("name").getAsString();
                actors.append(actorName).append(", ");
            }

            return actors.length() > 0 ? actors.substring(0, actors.length() - 2) : "No Actors";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching actors";
        }
    }

    /**
     * Obtiene nombres de películas similares utilizando la API de TMDb.
     *
     * @param movieId El ID de la película.
     * @return Una cadena con los nombres de las películas similares o un
     * mensaje de error.
     */
    private String getSimilarMovies(int movieId) {
        try {
            URL url = new URL("https://api.themoviedb.org/3/movie/" + movieId + "/similar?api_key=579950b8c5c2bb8f4979af5b90103b04&language=es-ES&page=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder response;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            StringBuilder similarMovies = new StringBuilder();
            for (JsonElement element : results) {
                JsonObject movie = element.getAsJsonObject();
                String movieTitle = getStringOrDefault(movie, "title", "No Title");
                similarMovies.append(movieTitle).append(", ");
            }

            return similarMovies.length() > 0 ? similarMovies.substring(0, similarMovies.length() - 2) : "No Similar Movies";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error fetching similar movies";
        }
    }

    /**
     * Obtiene un valor de una clave específica en un objeto JSON o un valor
     * predeterminado si la clave no existe.
     *
     * @param jsonObject El objeto JSON del cual se obtendrá el valor.
     * @param key La clave del valor deseado.
     * @param defaultValue El valor predeterminado si la clave no está presente.
     * @return El valor correspondiente a la clave o el valor predeterminado.
     */
    private String getStringOrDefault(JsonObject jsonObject, String key, String defaultValue) {
        JsonElement element = jsonObject.get(key);
        return (element != null && !element.isJsonNull()) ? element.getAsString() : defaultValue;
    }

    /**
     * Carga una imagen desde una URL y la redimensiona al tamaño especificado.
     *
     * @param imageUrl La URL de la imagen.
     * @param width El ancho deseado de la imagen.
     * @param height La altura deseada de la imagen.
     * @return La imagen redimensionada o null en caso de error.
     * @throws IOException Si hay un error al cargar la imagen.
     */
    private BufferedImage loadImage(String imageUrl, int width, int height) throws IOException {
        URL url = new URL("https://image.tmdb.org/t/p/w500" + imageUrl);
        BufferedImage originalImage = ImageIO.read(url);

        Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

        BufferedImage bufferedResizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = bufferedResizedImage.createGraphics();
        g2d.drawImage(resizedImage, 0, 0, width, height, null);
        g2d.dispose();

        return bufferedResizedImage;
    }

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args Los argumentos de la línea de comandos (no se utilizan en
     * este caso).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BuscarPeliculas();
        });
    }
}
