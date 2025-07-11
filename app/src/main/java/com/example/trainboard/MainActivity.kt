package com.example.trainboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.IconButton
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trainboard.ui.theme.TrainBoardTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(navController: NavHostController = rememberNavController()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    TrainBoardTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                }
            },
            topBar = {
                MyTopBar(
                    currentDestination = currentRoute ?: "",
                    showBackButtonDestinations = listOf("departures"),
                    onBackClick = {
                        navController.navigate("main") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    AppNavigation(Modifier.align(Alignment.Center), snackbarHostState = snackbarHostState, navController)
                }
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = Color(0xFF731522)
                ) {}

            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    currentDestination: String,
    showBackButtonDestinations: List<String>,
    onBackClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF731522),
            titleContentColor = Color.White
        ),
        title =  {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Live Departures"
            )
        },
        navigationIcon = {
            if (currentDestination in showBackButtonDestinations) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }
    )
}

@Composable
fun AppNavigation(modifier: Modifier, snackbarHostState: SnackbarHostState, navController: NavHostController) {
    val viewModel = ViewModel()
    NavHost(navController = navController, startDestination = "main") {
        composable("Main") {
            TrainSelectorScreen(modifier = modifier, snackbarHostState = snackbarHostState, navController) { journeys ->
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
    snackbarHostState: SnackbarHostState,
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
        Box(
            modifier = Modifier.align(Alignment.End)
        ) {
            SubmitButton(
                isEnabled = isButtonEnabled,
                snackbarHostState = snackbarHostState,
                navController,
                originCrs ?: "",
                destinationCrs ?: "",
                updateViewModel
            )
        }
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
fun SubmitButton(isEnabled: Boolean, snackbarHostState: SnackbarHostState, navController: NavController, originCrs: String, destinationCrs: String, updateViewModel: (List<Journey>) -> Unit) {
    var loading by remember { mutableStateOf(false) }
    val client = ApiClient()
    val coroutineScope = rememberCoroutineScope()
    Button(
        modifier = Modifier
            .padding(10.dp),
        enabled = isEnabled && !loading,
        onClick = {
            loading = true
            coroutineScope.launch {
                val fareResult = client.getFares(originCrs, destinationCrs)
                loading = false
                if (fareResult == null) {
                    snackbarHostState.showSnackbar("Error loading journeys. Please try again.")
                } else if (fareResult.outboundJourneys.isEmpty()) {
                    snackbarHostState.showSnackbar("No trains found. Please try a different route.")
                } else {
                    updateViewModel(fareResult.outboundJourneys)
                    navController.navigate("departures")
                }
            }
    }) {
        if (loading) IndeterminateCircularIndicator() else Text("Submit")
    }
}

@Composable
fun IndeterminateCircularIndicator() {
    CircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
fun FareDisplayScreen(journeys: List<Journey>) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(journeys) {
                FareDisplay(it)
            }
        }
    }
}
@Composable
fun FareDisplay(journey: Journey) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(10.dp)
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatTime(journey.departureTime))
                    Text(text = journey.originStation.crs, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "To",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatTime(journey.arrivalTime))
                    Text(text = journey.destinationStation.crs, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = formatDuration(journey.journeyDurationInMinutes))
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse tickets" else "Expand tickets"
                    )
                }
            }

            if (expanded) {
                if (journey.tickets.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("No tickets available")
                    }
                } else {
                    journey.tickets.forEach { ticket ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(text = ticket.name)
                            }
                            Box(
                                modifier = Modifier
                                    .wrapContentWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(text = formatPrice(ticket.priceInPennies))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatPrice(inputPriceInPennies: String): String {
    val numberOfPennies = inputPriceInPennies.toIntOrNull() ?: return "£0.00"
    val pounds = numberOfPennies / 100
    val remainderPennies = numberOfPennies % 100
    return "£${pounds}.${remainderPennies.toString().padStart(2, '0')}"
}

fun formatDuration(durationInMinutes: String): String {
    val durationInMinutesInteger = durationInMinutes.toIntOrNull() ?: return "0M"
    val hours = durationInMinutesInteger / 60
    val minutes = durationInMinutesInteger % 60
    return "${hours}h ${minutes}m"
}

fun formatTime(input: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.UK)
    val date = parser.parse(input)
    val formatter = SimpleDateFormat("HH:mm", Locale.UK)
    return formatter.format(date!!)
}