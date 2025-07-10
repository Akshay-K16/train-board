package com.example.trainboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trainboard.ui.theme.TrainBoardTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrainBoardTheme {
                Scaffold(
                    content = { paddingValues ->
                        Box(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            AppNavigation(Modifier.align(Alignment.Center))
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier, navController: NavHostController = rememberNavController()) {
    val viewModel = ViewModel()
    NavHost(navController = navController, startDestination = "main") {
        composable("Main") {
            TrainSelectorScreen(modifier = modifier, navController) { journeys ->
                viewModel.journeys = journeys
            }
        }
        composable(
            "departures"
        ) {
            FareDisplayScreen(viewModel.journeys)
        }
    }
}

@Composable
fun TrainSelectorScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    updateViewModel: (List<Journey>) -> Unit
) {
    var departureStation by remember { mutableStateOf<String?>(null) }
    var arrivalStation by remember { mutableStateOf<String?>(null) }
    val originCrs = StationInformation.getCrsFromName(departureStation)
    val destinationCrs = StationInformation.getCrsFromName(arrivalStation)
    val stationNames = StationInformation.getStationNames()
    val isButtonEnabled = originCrs != null && destinationCrs != null && originCrs != destinationCrs

    Column(modifier.padding(20.dp)) {
        StationDropdownMenu(
            departureStation,
            onStationSelected = { departureStation = it },
            stations = stationNames,
            dropdownLabel = "From")
        StationDropdownMenu(
            arrivalStation,
            onStationSelected = { arrivalStation = it },
            stations = stationNames,
            dropdownLabel = "To")
        SubmitButton(isEnabled = isButtonEnabled, navController, originCrs ?: "", destinationCrs ?: "", updateViewModel)
    }


}


@Composable
fun StationDropdownMenu(selectedStation: String?, onStationSelected: (String) -> Unit, stations: List<String>, dropdownLabel: String) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero)}
    val icon = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        OutlinedTextField(
            readOnly = true,
            value = selectedStation ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                },
            label = {Text(dropdownLabel)},
            trailingIcon = {
                Icon(icon,"Clickable arrow to open or close the dropdown menu",
                    Modifier.clickable { expanded = !expanded })
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){textFieldSize.width.toDp()})
        ) {
            stations.forEach { label ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        onStationSelected(label)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SubmitButton(isEnabled: Boolean, navController: NavController, originCrs: String, destinationCrs: String, updateViewModel: (List<Journey>) -> Unit) {
    val client = ApiClient()
    val coroutineScope = rememberCoroutineScope()
    Button(
        enabled = isEnabled,
        onClick = {
            coroutineScope.launch {
                val fareResult = client.getFares(originCrs, destinationCrs)
                updateViewModel(fareResult.outboundJourneys)
                navController.navigate("departures")
            }
    }) {
        Text("Submit")
    }
}

@Composable
fun FareDisplayScreen(journeys: List<Journey>) {
    Text("Live Departures")
    journeys.forEach {
        print(it.originStation.displayName)
        print(" -- ")
        print(it.destinationStation.displayName)
        print(" (")
        print(it.departureTime)
        println(")")
    }

}