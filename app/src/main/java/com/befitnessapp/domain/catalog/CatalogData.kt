package com.befitnessapp.domain.catalog

// ====== IDs de Grupos ======
const val GRUPO_ESPALDA = 11
const val GRUPO_PECHO   = 12
const val GRUPO_HOMBROS = 13
const val GRUPO_PIERNAS = 14
const val GRUPO_GLUTEOS = 15
const val GRUPO_BRAZOS  = 16
const val GRUPO_CORE    = 17

// ====== Músculos (IDs canónicos/porciones) ======
// Espalda (11)
const val M_DORSAL_ANCHO     = 1100
const val M_ROMBOIDES        = 1101
const val M_TRAPECIO         = 1102
const val M_TRAPECIO_MEDIO   = 1103 // parent: 1102
const val M_TRAPECIO_SUPERIOR= 1104 // parent: 1102
const val M_ERECTORES        = 1105

// Pecho (12)
const val M_PECTORAL         = 1200
const val M_PEC_SUPERIOR     = 1201 // parent: 1200
const val M_PEC_MEDIO        = 1202 // parent: 1200
const val M_PEC_INFERIOR     = 1203 // parent: 1200

// Hombros (13)
const val M_DELTOIDE         = 1300
const val M_DEL_ANTERIOR     = 1301 // parent: 1300
const val M_DEL_LATERAL      = 1302 // parent: 1300
const val M_DEL_POSTERIOR    = 1303 // parent: 1300

// Piernas (14)
const val M_CUADRICEPS       = 1400
const val M_ISQUIOS          = 1401
const val M_PANTORRILLAS     = 1402
const val M_GASTROCNEMIO     = 1403 // parent: 1402
const val M_SOLEO            = 1404 // parent: 1402

// Glúteos (15)
const val M_GLUTEO_MAYOR     = 1500
const val M_GLUTEO_MEDIO     = 1501

// Brazos (16)
const val M_BICEPS           = 1600
const val M_BICEPS_LARGO     = 1601 // parent: 1600
const val M_BICEPS_CORTO     = 1602 // parent: 1600
const val M_TRICEPS          = 1603
const val M_TRI_LARGO        = 1604 // parent: 1603
const val M_TRI_MEDIAL       = 1605 // parent: 1603
const val M_TRI_LATERAL      = 1606 // parent: 1603

// Core (17)
const val M_RECTO_ABDOMINAL  = 1700
const val M_OBLICUOS         = 1701

// ====== Listas base ======
val groups: List<MuscleGroup> = listOf(
    MuscleGroup(GRUPO_ESPALDA, "Espalda"),
    MuscleGroup(GRUPO_PECHO,   "Pecho"),
    MuscleGroup(GRUPO_HOMBROS, "Hombros"),
    MuscleGroup(GRUPO_PIERNAS, "Piernas"),
    MuscleGroup(GRUPO_GLUTEOS, "Glúteos"),
    MuscleGroup(GRUPO_BRAZOS,  "Brazos"),
    MuscleGroup(GRUPO_CORE,    "Core"),
)

val muscles: List<Muscle> = listOf(
    // Espalda
    Muscle(M_DORSAL_ANCHO, GRUPO_ESPALDA, "Dorsal ancho"),
    Muscle(M_ROMBOIDES, GRUPO_ESPALDA, "Romboides"),
    Muscle(M_TRAPECIO, GRUPO_ESPALDA, "Trapecio"),
    Muscle(M_TRAPECIO_MEDIO, GRUPO_ESPALDA, "Trapecio medio", parentId = M_TRAPECIO),
    Muscle(M_TRAPECIO_SUPERIOR, GRUPO_ESPALDA, "Trapecio superior", parentId = M_TRAPECIO),
    Muscle(M_ERECTORES, GRUPO_ESPALDA, "Erectores espinales"),

    // Pecho
    Muscle(M_PECTORAL, GRUPO_PECHO, "Pectoral"),
    Muscle(M_PEC_SUPERIOR, GRUPO_PECHO, "Pectoral superior", parentId = M_PECTORAL),
    Muscle(M_PEC_MEDIO, GRUPO_PECHO, "Pectoral medio", parentId = M_PECTORAL),
    Muscle(M_PEC_INFERIOR, GRUPO_PECHO, "Pectoral inferior", parentId = M_PECTORAL),

    // Hombros
    Muscle(M_DELTOIDE, GRUPO_HOMBROS, "Deltoide"),
    Muscle(M_DEL_ANTERIOR, GRUPO_HOMBROS, "Deltoide anterior", parentId = M_DELTOIDE),
    Muscle(M_DEL_LATERAL, GRUPO_HOMBROS, "Deltoide lateral", parentId = M_DELTOIDE),
    Muscle(M_DEL_POSTERIOR, GRUPO_HOMBROS, "Deltoide posterior", parentId = M_DELTOIDE),

    // Piernas
    Muscle(M_CUADRICEPS, GRUPO_PIERNAS, "Cuádriceps"),
    Muscle(M_ISQUIOS, GRUPO_PIERNAS, "Isquiosurales"),
    Muscle(M_PANTORRILLAS, GRUPO_PIERNAS, "Pantorrillas"),
    Muscle(M_GASTROCNEMIO, GRUPO_PIERNAS, "Gastrocnemio", parentId = M_PANTORRILLAS),
    Muscle(M_SOLEO, GRUPO_PIERNAS, "Sóleo", parentId = M_PANTORRILLAS),

    // Glúteos
    Muscle(M_GLUTEO_MAYOR, GRUPO_GLUTEOS, "Glúteo mayor"),
    Muscle(M_GLUTEO_MEDIO, GRUPO_GLUTEOS, "Glúteo medio"),

    // Brazos
    Muscle(M_BICEPS, GRUPO_BRAZOS, "Bíceps"),
    Muscle(M_BICEPS_LARGO, GRUPO_BRAZOS, "Bíceps cabeza larga", parentId = M_BICEPS),
    Muscle(M_BICEPS_CORTO, GRUPO_BRAZOS, "Bíceps cabeza corta", parentId = M_BICEPS),
    Muscle(M_TRICEPS, GRUPO_BRAZOS, "Tríceps"),
    Muscle(M_TRI_LARGO, GRUPO_BRAZOS, "Tríceps cabeza larga", parentId = M_TRICEPS),
    Muscle(M_TRI_MEDIAL, GRUPO_BRAZOS, "Tríceps medial", parentId = M_TRICEPS),
    Muscle(M_TRI_LATERAL, GRUPO_BRAZOS, "Tríceps lateral", parentId = M_TRICEPS),

    // Core
    Muscle(M_RECTO_ABDOMINAL, GRUPO_CORE, "Recto abdominal"),
    Muscle(M_OBLICUOS, GRUPO_CORE, "Oblicuos"),
)

// ====== Ejercicios (12) ======
private fun rr(h: IntRange, f: IntRange, r: IntRange) = mapOf(
    RepGoal.HIPERTROFIA to RepRange(h.first, h.last),
    RepGoal.FUERZA to RepRange(f.first, f.last),
    RepGoal.RESISTENCIA to RepRange(r.first, r.last),
)

val exercises: List<Exercise> = listOf(
    Exercise(
        id = 12001,
        name = "Bench press plano (barra)",
        pattern = Pattern.PRESS,
        repRangeByGoal = rr(8..12, 3..6, 12..20),
        contraindications = listOf("hombro"),
        targets = listOf(
            ExerciseTarget(M_PEC_MEDIO, 0.80, TargetRole.PRIMARY),
            ExerciseTarget(M_PEC_SUPERIOR, 0.20, TargetRole.SECONDARY),
            ExerciseTarget(M_TRI_LATERAL, 0.50, TargetRole.SECONDARY),
            ExerciseTarget(M_TRI_MEDIAL, 0.50, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 12002,
        name = "Press inclinado (mancuernas)",
        pattern = Pattern.PRESS,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        contraindications = listOf("hombro"),
        targets = listOf(
            ExerciseTarget(M_PEC_SUPERIOR, 0.80, TargetRole.PRIMARY),
            ExerciseTarget(M_PEC_MEDIO, 0.20, TargetRole.SECONDARY),
            ExerciseTarget(M_TRI_LATERAL, 0.50, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 11001,
        name = "Remo con barra",
        pattern = Pattern.ROW,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        contraindications = listOf("lumbar"),
        targets = listOf(
            ExerciseTarget(M_DORSAL_ANCHO, 0.60, TargetRole.PRIMARY),
            ExerciseTarget(M_ROMBOIDES, 0.40, TargetRole.PRIMARY),
            ExerciseTarget(M_TRAPECIO_MEDIO, 0.40, TargetRole.SECONDARY),
            ExerciseTarget(M_BICEPS, 0.50, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 11002,
        name = "Jalón al pecho (polea)",
        pattern = Pattern.PULL,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        targets = listOf(
            ExerciseTarget(M_DORSAL_ANCHO, 0.70, TargetRole.PRIMARY),
            ExerciseTarget(M_ROMBOIDES, 0.30, TargetRole.SECONDARY),
            ExerciseTarget(M_BICEPS, 0.50, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 13001,
        name = "Press militar",
        pattern = Pattern.OVERHEAD,
        repRangeByGoal = rr(8..12, 3..6, 12..15),
        contraindications = listOf("hombro"),
        targets = listOf(
            ExerciseTarget(M_DEL_ANTERIOR, 0.60, TargetRole.PRIMARY),
            ExerciseTarget(M_DEL_LATERAL, 0.40, TargetRole.PRIMARY),
            ExerciseTarget(M_TRI_LATERAL, 0.50, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 13002,
        name = "Elevaciones laterales",
        pattern = Pattern.RAISE,
        repRangeByGoal = rr(10..15, 6..8, 15..20),
        targets = listOf(
            ExerciseTarget(M_DEL_LATERAL, 1.00, TargetRole.PRIMARY),
        )
    ),
    Exercise(
        id = 14001,
        name = "Sentadilla trasera",
        pattern = Pattern.SQUAT,
        repRangeByGoal = rr(6..10, 3..5, 12..15),
        contraindications = listOf("rodilla", "lumbar"),
        targets = listOf(
            ExerciseTarget(M_CUADRICEPS, 0.50, TargetRole.PRIMARY),
            ExerciseTarget(M_GLUTEO_MAYOR, 0.30, TargetRole.SECONDARY),
            ExerciseTarget(M_ERECTORES, 0.20, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 14002,
        name = "Peso muerto rumano (RDL)",
        pattern = Pattern.HINGE,
        repRangeByGoal = rr(6..10, 3..5, 10..12),
        contraindications = listOf("lumbar"),
        targets = listOf(
            ExerciseTarget(M_ISQUIOS, 0.60, TargetRole.PRIMARY),
            ExerciseTarget(M_GLUTEO_MAYOR, 0.30, TargetRole.SECONDARY),
            ExerciseTarget(M_ERECTORES, 0.10, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 14003,
        name = "Elevación de talones (de pie)",
        pattern = Pattern.CALF,
        repRangeByGoal = rr(10..15, 6..8, 15..20),
        targets = listOf(
            ExerciseTarget(M_GASTROCNEMIO, 0.70, TargetRole.PRIMARY),
            ExerciseTarget(M_SOLEO, 0.30, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 15001,
        name = "Hip thrust",
        pattern = Pattern.THRUST,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        targets = listOf(
            ExerciseTarget(M_GLUTEO_MAYOR, 0.70, TargetRole.PRIMARY),
            ExerciseTarget(M_ISQUIOS, 0.20, TargetRole.SECONDARY),
            ExerciseTarget(M_CUADRICEPS, 0.10, TargetRole.SECONDARY),
        )
    ),
    Exercise(
        id = 16001,
        name = "Curl de bíceps (barra)",
        pattern = Pattern.CURL,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        targets = listOf(
            ExerciseTarget(M_BICEPS_LARGO, 0.60, TargetRole.PRIMARY),
            ExerciseTarget(M_BICEPS_CORTO, 0.40, TargetRole.PRIMARY),
        )
    ),
    Exercise(
        id = 16002,
        name = "Jalón de tríceps (polea)",
        pattern = Pattern.EXTENSION,
        repRangeByGoal = rr(8..12, 4..6, 12..15),
        targets = listOf(
            ExerciseTarget(M_TRI_LATERAL, 0.50, TargetRole.PRIMARY),
            ExerciseTarget(M_TRI_MEDIAL, 0.30, TargetRole.SECONDARY),
            ExerciseTarget(M_TRI_LARGO, 0.20, TargetRole.SECONDARY),
        )
    ),
)
