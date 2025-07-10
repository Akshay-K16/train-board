package com.example.trainboard

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.LocalContext
import com.example.trainboard.ui.theme.TrainBoardTheme
import androidx.core.net.toUri

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
                            TrainSelectorScreen(modifier = Modifier.align(Alignment.Center))

                        }
                    },
                )
            }
        }
    }
}

@Composable
fun TrainSelectorScreen(modifier: Modifier = Modifier) {
    var departureStation by remember { mutableStateOf<String?>(null) }
    var arrivalStation by remember { mutableStateOf<String?>(null) }
    val originCrs = StationInformation.getCrsFromName(departureStation)
    val destinationCrs = StationInformation.getCrsFromName(arrivalStation)
    val stationNames = StationInformation.getStationNames()
    val isButtonEnabled = originCrs != null && destinationCrs != null && originCrs != destinationCrs
    val uri = createURI(
        originCrs ?: "",
        destinationCrs ?: ""
    )

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
        SubmitButton(uri = uri, isEnabled = isButtonEnabled)
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
fun SubmitButton(uri: Uri, isEnabled: Boolean) {
    val context = LocalContext.current
    Button(
        enabled = isEnabled,
        onClick = {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }) {
        Text("View Live Departures ")
    }

}

fun createURI(toStation: String, fromStation: String): Uri {
   return "https://www.lner.co.uk/travel-information/travelling-now/live-train-times/depart/$toStation/$fromStation/#LiveDepResults".toUri()
}