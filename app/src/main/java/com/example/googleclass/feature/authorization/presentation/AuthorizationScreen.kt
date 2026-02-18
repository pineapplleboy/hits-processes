package com.example.googleclass.feature.authorization.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleclass.R
import com.example.googleclass.common.presentation.component.LoadingState
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.common.presentation.theme.MediumGray
import com.example.googleclass.common.presentation.theme.Outline
import com.example.googleclass.feature.authorization.domain.model.UserCredentials
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthorizationScreen() {
    val viewModel: AuthorizationScreenViewModel = koinViewModel()
    val screenState by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (val state = screenState) {
            is AuthorizationScreenState.Default -> DefaultState(
                credentials = state.credentials,
                onLoginClick = viewModel::onLoginClick,
                onLoginChange = viewModel::onLoginChange,
                onPasswordChange = viewModel::onPasswordChange,
                modifier = Modifier.padding(padding)
            )

            AuthorizationScreenState.Loading -> LoadingState()
        }
    }
}

@Composable
private fun DefaultState(
    credentials: UserCredentials,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier,
) = Column(
    modifier = modifier
        .fillMaxSize()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var registerPasswordVisible by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var registrationEmail by remember { mutableStateOf("") }
    var registrationPassword by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf(AuthTab.Login) }

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            TabRow(selectedTabIndex = selectedTab.ordinal) {
                Tab(
                    selected = selectedTab == AuthTab.Login,
                    onClick = { selectedTab = AuthTab.Login },
                    text = { Text(text = stringResource(R.string.tab_login)) },
                )
                Tab(
                    selected = selectedTab == AuthTab.Register,
                    onClick = { selectedTab = AuthTab.Register },
                    text = { Text(text = stringResource(R.string.tab_register)) },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (selectedTab) {
                AuthTab.Login -> LoginFields(
                    credentials = credentials,
                    passwordVisible = loginPasswordVisible,
                    onLoginChange = onLoginChange,
                    onPasswordChange = onPasswordChange,
                    onPasswordVisibilityChange = { loginPasswordVisible = !loginPasswordVisible },
                    onLoginClick = onLoginClick,
                )

                AuthTab.Register -> RegisterFields(
                    fullName = fullName,
                    birthDate = birthDate,
                    city = city,
                    registrationEmail = registrationEmail,
                    registrationPassword = registrationPassword,
                    onFullNameChange = { fullName = it },
                    onBirthDateChange = { birthDate = it },
                    onCityChange = { city = it },
                    onRegistrationEmailChange = { registrationEmail = it },
                    onRegistrationPasswordChange = { registrationPassword = it },
                    passwordVisible = registerPasswordVisible,
                    onPasswordVisibilityChange = { registerPasswordVisible = !registerPasswordVisible },
                )
            }
        }
    }
}

@Composable
private fun LoginFields(
    credentials: UserCredentials,
    passwordVisible: Boolean,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityChange: () -> Unit,
    onLoginClick: () -> Unit,
) {
    OutlinedTextField(
        value = credentials.login,
        onValueChange = onLoginChange,
        label = { Text(stringResource(R.string.email)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = credentials.password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.password)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            TextButton(onClick = onPasswordVisibilityChange) {
                Text(
                    if (passwordVisible)
                        stringResource(R.string.hide_password)
                    else
                        stringResource(R.string.show_password)
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onLoginClick,
        enabled = credentials.login.isNotBlank() && credentials.password.isNotBlank(),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = Outline,
            disabledContentColor = MediumGray
        )
    ) {
        Text(
            text = stringResource(R.string.login_button),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun RegisterFields(
    fullName: String,
    birthDate: String,
    city: String,
    registrationEmail: String,
    registrationPassword: String,
    onFullNameChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onRegistrationEmailChange: (String) -> Unit,
    onRegistrationPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
) {
    val isFullNameValid = remember(fullName) { isFullNameValid(fullName) }
    val isFormValid =
        isFullNameValid &&
                birthDate.isNotBlank() &&
                city.isNotBlank() &&
                registrationEmail.isNotBlank() &&
                registrationPassword.isNotBlank()

    OutlinedTextField(
        value = fullName,
        onValueChange = onFullNameChange,
        label = { Text(stringResource(R.string.full_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        isError = fullName.isNotBlank() && !isFullNameValid,
        supportingText = {
            if (fullName.isNotBlank() && !isFullNameValid) {
                Text(text = stringResource(R.string.full_name_error))
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = birthDate,
        onValueChange = onBirthDateChange,
        label = { Text(stringResource(R.string.birth_date)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = city,
        onValueChange = onCityChange,
        label = { Text(stringResource(R.string.city)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = registrationEmail,
        onValueChange = onRegistrationEmailChange,
        label = { Text(stringResource(R.string.email)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = registrationPassword,
        onValueChange = onRegistrationPasswordChange,
        label = { Text(stringResource(R.string.password)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (passwordVisible)
            VisualTransformation.None
        else
            PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        trailingIcon = {
            TextButton(onClick = onPasswordVisibilityChange) {
                Text(
                    if (passwordVisible)
                        stringResource(R.string.hide_password)
                    else
                        stringResource(R.string.show_password)
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {

        },
        enabled = isFormValid,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
            disabledContainerColor = Outline,
            disabledContentColor = MediumGray
        )
    ) {
        Text(
            text = stringResource(R.string.register_button),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private enum class AuthTab {
    Login,
    Register,
}

private fun isFullNameValid(fullName: String): Boolean {
    val parts = fullName.trim().split("\\s+".toRegex())
    if (parts.size != 2) return false

    val name = parts[0]
    val surname = parts[1]
    val regex = Regex("^[А-Яа-яЁё]{2,}$")

    return regex.matches(name) && regex.matches(surname)
}

@Preview(showBackground = true)
@Composable
private fun AuthorizationScreenPreview() {
    GoogleClassTheme {
        AuthorizationScreen()
    }
}
