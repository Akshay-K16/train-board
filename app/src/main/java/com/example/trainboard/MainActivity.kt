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
    val stationCodesMap = StationInformation.stationCodesMap
    val isButtonEnabled = departureStation != null && arrivalStation != null && arrivalStation != departureStation

    Column(modifier.padding(20.dp)) {
        StationDropdownMenu(
            departureStation,
            onStationSelected = { departureStation = it },
            stations = ArrayList(stationCodesMap.keys),
            dropdownLabel = "From")
        StationDropdownMenu(
            arrivalStation,
            onStationSelected = { arrivalStation = it },
            stations = ArrayList(stationCodesMap.keys),
            dropdownLabel = "To")
        SubmitButton(isEnabled = isButtonEnabled)
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
fun SubmitButton(isEnabled: Boolean) {
    val client = ApiClient()
    val coroutineScope = rememberCoroutineScope()
    Button(
        enabled = isEnabled,
        onClick = {
            coroutineScope.launch {
                client.sendRequest()
            }
    }) {
        Text("View Live Departures ")
    }

}