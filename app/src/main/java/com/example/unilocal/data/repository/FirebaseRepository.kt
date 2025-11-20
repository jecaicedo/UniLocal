package com.example.unilocal.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import com.example.unilocal.data.model.*
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // AUTH
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Usuario no encontrado")
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)?.copy(id = userId)
                ?: throw Exception("Datos de usuario no encontrados")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, nombre: String, username: String, ciudad: String): Result<User> {
        return try {
            // Verificar username único
            val usernameExists = firestore.collection("users")
                .whereEqualTo("username", username).get().await()
            if (!usernameExists.isEmpty) {
                throw Exception("El username ya existe")
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Error al crear usuario")

            val user = User(
                id = userId,
                nombre = nombre,
                username = username,
                email = email,
                ciudad = ciudad,
                rol = "usuario"
            )

            firestore.collection("users").document(userId).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getCurrentUser(): User? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            doc.toObject(User::class.java)?.copy(id = userId)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // LUGARES
    suspend fun crearLugar(lugar: Lugar): Result<String> {
        return try {
            val docRef = firestore.collection("lugares").document()
            val nuevoLugar = lugar.copy(
                id = docRef.id,
                fechaCreacion = Timestamp.now()
            )
            docRef.set(nuevoLugar).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLugaresAprobados(): List<Lugar> {
        return try {
            firestore.collection("lugares")
                .whereEqualTo("estado", "aprobado")
                .get().await()
                .documents.mapNotNull { it.toObject(Lugar::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLugaresPendientes(): List<Lugar> {
        return try {
            firestore.collection("lugares")
                .whereEqualTo("estado", "pendiente")
                .get().await()
                .documents.mapNotNull { it.toObject(Lugar::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLugaresAprobadosPorModerador(moderadorId: String): List<Lugar> {
        return try {
            firestore.collection("lugares")
                .whereEqualTo("estado", "aprobado")
                .whereEqualTo("moderadorId", moderadorId)
                .get().await()
                .documents.mapNotNull { it.toObject(Lugar::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getLugaresPorUsuario(usuarioId: String): List<Lugar> {
        return try {
            firestore.collection("lugares")
                .whereEqualTo("creadoPor", usuarioId)
                .get().await()
                .documents.mapNotNull { it.toObject(Lugar::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun aprobarLugar(lugarId: String, moderadorId: String): Result<Unit> {
        return try {
            firestore.collection("lugares").document(lugarId)
                .update(mapOf(
                    "estado" to "aprobado",
                    "moderadorId" to moderadorId
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rechazarLugar(lugarId: String, moderadorId: String): Result<Unit> {
        return try {
            firestore.collection("lugares").document(lugarId)
                .update(mapOf(
                    "estado" to "rechazado",
                    "moderadorId" to moderadorId
                )).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarLugar(lugarId: String): Result<Unit> {
        return try {
            firestore.collection("lugares").document(lugarId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // COMENTARIOS
    suspend fun agregarComentario(comentario: Comentario): Result<Unit> {
        return try {
            val docRef = firestore.collection("comentarios").document()
            val nuevoComentario = comentario.copy(
                id = docRef.id,
                fecha = Timestamp.now()
            )
            docRef.set(nuevoComentario).await()

            // Forzar actualización inmediata
            actualizarCalificacionPromedio(comentario.lugarId)

            // Log para verificar
            println("✅ Comentario guardado: ${docRef.id}")

            Result.success(Unit)
        } catch (e: Exception) {
            println("❌ Error al guardar comentario: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getComentariosPorLugar(lugarId: String): List<Comentario> {
        return try {
            println("🔍 Buscando comentarios para lugar: $lugarId")

            val comentarios = firestore.collection("comentarios")
                .whereEqualTo("lugarId", lugarId)
                .get().await()
                .documents.mapNotNull { doc ->
                    println("📄 Documento encontrado: ${doc.id}")
                    doc.toObject(Comentario::class.java)?.copy(id = doc.id)
                }

            println("✅ Total comentarios encontrados: ${comentarios.size}")
            comentarios.sortedByDescending { it.fecha?.seconds ?: 0 }
        } catch (e: Exception) {
            println("❌ Error al obtener comentarios: ${e.message}")
            emptyList()
        }
    }

    suspend fun responderComentario(comentarioId: String, respuesta: String): Result<Unit> {
        return try {
            firestore.collection("comentarios").document(comentarioId)
                .update("respuesta", respuesta).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun actualizarCalificacionPromedio(lugarId: String) {
        try {
            val comentarios = getComentariosPorLugar(lugarId)
            if (comentarios.isNotEmpty()) {
                val promedio = comentarios.map { it.calificacion }.average()
                firestore.collection("lugares").document(lugarId)
                    .update("calificacionPromedio", promedio).await()
            }
        } catch (e: Exception) {
            // Ignorar error
        }
    }

    // FAVORITOS
    suspend fun agregarFavorito(usuarioId: String, lugarId: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(usuarioId)
            val user = userRef.get().await().toObject(User::class.java)
            val favoritos = user?.favoritos?.toMutableList() ?: mutableListOf()
            if (!favoritos.contains(lugarId)) {
                favoritos.add(lugarId)
                userRef.update("favoritos", favoritos).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarFavorito(usuarioId: String, lugarId: String): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(usuarioId)
            val user = userRef.get().await().toObject(User::class.java)
            val favoritos = user?.favoritos?.toMutableList() ?: mutableListOf()
            favoritos.remove(lugarId)
            userRef.update("favoritos", favoritos).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLugaresFavoritos(usuarioId: String): List<Lugar> {
        return try {
            val user = firestore.collection("users").document(usuarioId).get().await()
                .toObject(User::class.java)
            val favoritoIds = user?.favoritos ?: emptyList()

            if (favoritoIds.isEmpty()) return emptyList()

            favoritoIds.mapNotNull { id ->
                firestore.collection("lugares").document(id).get().await()
                    .toObject(Lugar::class.java)?.copy(id = id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // STORAGE - IMÁGENES
    suspend fun subirImagen(uri: Uri): Result<String> {
        return try {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("imagenes/$filename")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // BÚSQUEDA
    suspend fun buscarLugares(query: String, categoria: String?): List<Lugar> {
        return try {
            val lugares = getLugaresAprobados()
            lugares.filter { lugar ->
                val matchNombre = lugar.nombre.contains(query, ignoreCase = true)
                val matchCategoria = categoria == null || lugar.categoria == categoria
                matchNombre && matchCategoria
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun logout() {
        auth.signOut()
    }
}