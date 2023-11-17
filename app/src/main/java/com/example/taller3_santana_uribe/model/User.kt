package com.example.taller3_santana_uribe.model

import java.io.Serializable

class User(var nombre: String? = null,
           var apellido: String? = null,
           var email: String? = null,
           var password: String? = null,
           var activo: Boolean = false,
           var imageUrl: String? = null,
           var latitude : Double = 0.0,
           var longitude : Double = 0.0
): Serializable

