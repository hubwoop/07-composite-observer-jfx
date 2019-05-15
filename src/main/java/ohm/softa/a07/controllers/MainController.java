package ohm.softa.a07.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import ohm.softa.a07.api.OpenMensaAPI;
import ohm.softa.a07.model.Meal;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {

	// use annotation to tie to component in XML
	@FXML
	private Button btnRefresh;

	@FXML
	private Button btnClose;

	@FXML
	private ListView<String> mealsList;

	@FXML
	private CheckBox chkVegetarian;

	private ObservableList<String> list;

	private OpenMensaAPI openMensaAPI;

	private Callback<List<Meal>> fetchCallback;
	private List<Meal> fetchedMeals;
	private List<String> filteredMealsDisplay;
	private List<String> fetchedMealsDisplay;


	@Override
	public void initialize(URL location, ResourceBundle resources) {

		OkHttpClient client = new OkHttpClient.Builder()
			.build();

		openMensaAPI = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("https://openmensa.org/api/v2/")
			.client(client)
			.build()
			.create(OpenMensaAPI.class);

		btnRefresh.setOnAction(event -> {
			LocalDateTime ldt = LocalDateTime.now();
			DateTimeFormatter format = DateTimeFormatter.ISO_DATE;
			Call<List<Meal>> mealCall = openMensaAPI.getMeals(format.format(ldt));
			mealCall.enqueue(fetchCallback);
		});

		btnClose.setOnAction(event -> list.clear());

		chkVegetarian.selectedProperty().addListener(
			(observable, oldValue, selected) -> {
				System.out.println(selected);
				if(selected) {
					filteredMealsDisplay = fetchedMeals.stream()
						.filter(Meal::isVegetarian)
						.map(Meal::toString)
						.collect(Collectors.toList());
					list.clear();
					list.addAll(filteredMealsDisplay);
				} else {
					list.clear();
					list.addAll(fetchedMealsDisplay);
				}

			}
		);

		fetchCallback = new Callback<List<Meal>>() {
			@Override
			public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response) {
				if(response.isSuccessful()) {
					List<Meal> meals = response.body();
					if(meals != null) {
						processFetchResult(meals);
						fetchedMeals = meals;
					}
				}
			}

			private void processFetchResult(List<Meal> meals) {
				fetchedMealsDisplay = meals.stream().map(Meal::toString).collect(Collectors.toList());
				Platform.runLater(() -> {
					list = FXCollections.observableArrayList(fetchedMealsDisplay);
					mealsList.setItems(list);
				});
			}

			@Override
			public void onFailure(Call<List<Meal>> call, Throwable t) {
				System.out.println("fail");
			}
		};
	}
}
