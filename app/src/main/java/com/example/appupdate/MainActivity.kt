package com.example.appupdate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.appupdate.ui.theme.AppUpdateTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    // Inicializa o Firestore
    val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppUpdateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(db)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(db: FirebaseFirestore) {
    var nome by remember { mutableStateOf("") } // Estado para armazenar o nome
    var telefone by remember { mutableStateOf("") } // Estado para armazenar o telefone
    var clientes by remember { mutableStateOf(listOf<Map<String, String>>()) } // Estado para armazenar a lista de clientes

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally // Centraliza o conteúdo horizontalmente
    ) {
        // Exibe o nome e a turma do usuário um acima do outro
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Nome: Julia")
            Text(text = "Turma: 3DS")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Adiciona uma imagem personalizada
        val image: Painter = painterResource(id = R.drawable.images) // Certifique-se de que a imagem existe no drawable
        Image(painter = image, contentDescription = "Imagem personalizada", modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        // Centraliza os campos de entrada e o botão
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Campo para entrada do nome com bordas arredondadas
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(0.3f)) {
                    Text(text = "Nome:")
                }
                Column {
                    TextField(
                        value = nome,
                        onValueChange = { nome = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp) // Borda arredondada
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Campo para entrada do telefone com bordas arredondadas
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(0.3f)) {
                    Text(text = "Telefone:")
                }
                Column {
                    TextField(
                        value = telefone,
                        onValueChange = { telefone = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp) // Borda arredondada
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botão para cadastrar cliente com cor vermelha
            Button(
                onClick = {
                    if (nome.isNotEmpty() && telefone.isNotEmpty()) { // Verifica se os campos não estão vazios
                        val pessoa = hashMapOf(
                            "nome" to nome,
                            "telefone" to telefone
                        )
                        // Adiciona cliente ao Firestore
                        db.collection("Clientes").add(pessoa)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Firestore", "Cliente adicionado com sucesso: ${documentReference.id}")
                                nome = "" // Limpa o campo de nome após o cadastro
                                telefone = "" // Limpa o campo de telefone após o cadastro
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Erro ao adicionar cliente", e)
                            }
                    }
                },
                modifier = Modifier
                    .width(200.dp) // Define uma largura fixa para o botão
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Define a cor do botão como vermelha
            ) {
                Text(text = "Cadastrar")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Exibe a lista de clientes cadastrados
        LaunchedEffect(Unit) {
            db.collection("Clientes")
                .get()
                .addOnSuccessListener { documents ->
                    val listaClientes = mutableListOf<Map<String, String>>()
                    for (document in documents) {
                        listaClientes.add(
                            mapOf(
                                "id" to document.id,
                                "nome" to "${document.data["nome"]}",
                                "telefone" to "${document.data["telefone"]}"
                            )
                        )
                    }
                    clientes = listaClientes // Atualiza a lista de clientes
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Erro ao buscar clientes: ", exception)
                }
        }

        // LazyColumn para exibir os clientes cadastrados
        LazyColumn {
            items(clientes) { cliente ->
                Row(Modifier.fillMaxWidth().padding(8.dp)) {
                    Column(Modifier.weight(0.5f)) {
                        Text(text = "Nome: ${cliente["nome"]}")
                    }
                    Column(Modifier.weight(0.5f)) {
                        Text(text = "Telefone: ${cliente["telefone"]}")
                    }
                }
            }
        }
    }
}
