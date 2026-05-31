package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.content.Context

// ==========================================
// 1. ENTITIES
// ==========================================

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val mobile: String,
    val email: String,
    val vehicleType: String,
    val services: String, // Comma-separated list details
    val dateString: String,
    val timeSlot: String,
    val status: String, // Pending, Confirmed, In Progress, Completed, Cancelled
    val bookingId: String, // Custom formatted: RX-XXXXX
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val discountText: String,
    val promoCode: String,
    val bannerIndex: Int = 0, // Maps to pre-styled slides if needed
    val isActive: Boolean = true
)

@Entity(tableName = "gallery_items")
data class GalleryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Automatic Wash, Foam Wash, Detailing, Interior Cleaning
    val title: String,
    val description: String,
    val drawableResId: String // Locally resolved style or vector representation
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reviewerName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val vehicleModel: String, // String representation info
    val timestamp: Long = System.currentTimeMillis()
)

// ==========================================
// 2. DATA ACCESS OBJECT (DAO)
// ==========================================

@Dao
interface CarWashDao {
    // Bookings
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE mobile LIKE '%' || :mobile || '%' ORDER BY timestamp DESC")
    fun searchBookingsByMobile(mobile: String): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("UPDATE bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Int, status: String)

    @Query("DELETE FROM bookings WHERE id = :id")
    suspend fun deleteBooking(id: Int)

    // Offers
    @Query("SELECT * FROM offers ORDER BY id DESC")
    fun getAllOffers(): Flow<List<Offer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: Offer)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOffer(id: Int)

    // Gallery
    @Query("SELECT * FROM gallery_items ORDER BY id DESC")
    fun getAllGalleryItems(): Flow<List<GalleryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGalleryItem(item: GalleryItem)

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteGalleryItem(id: Int)

    // Reviews
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)
}

// ==========================================
// 3. DATABASE HOLDER & CREATOR
// ==========================================

@Database(
    entities = [Booking::class, Offer::class, GalleryItem::class, Review::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carWashDao(): CarWashDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "xpress_carwash_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Inject default static data asynchronously when database is created.
                        db.execSQL("INSERT INTO offers (title, description, discountText, promoCode, bannerIndex, isActive) VALUES " +
                                "('Foam Wash Deluxe Special', 'Get high gloss finish foam wash premium detailing in Lucknow.', '20% OFF', 'FOAM20', 1, 1), " +
                                "('Weekend Cleaning Combo', 'Free deep cabin interior vacuuming and carpet dry-wash on standard packages.', 'FREE VACUUM', 'CLEANWEEK', 2, 1), " +
                                "('Ceramic Paint Protect Shield', 'Long lasting Teflon and ultra-gloss ceramic sealing layers.', '₹500 DISCOUNT', 'SHIELD500', 3, 1), " +
                                "('Alloy Wheel Detail Shine', 'Special micro-scratch brake dust removal treatments.', '15% SAVINGS', 'WHEEL15', 4, 1)")

                        db.execSQL("INSERT INTO gallery_items (category, title, description, drawableResId) VALUES " +
                                "('Automatic Wash', 'Robotic Tunnel Wash', 'Scratch free smart conveyor mechanism washing fully active.', 'ic_automatic_wash'), " +
                                "('Automatic Wash', 'Dry Blowers Jet Sparkle', 'Advanced 40HP storm dry blower tunnels.', 'ic_dryer'), " +
                                "('Foam Wash', 'Premium Foam Bath', 'Active snow lather spray breaking deep dirt bonds.', 'ic_foam_bath'), " +
                                "('Foam Wash', 'High-Pressure Jet Spray', '350 Bar powerful precision water nozzles cleaning action.', 'ic_pressure_wash'), " +
                                "('Detailing', 'Teflon Gloss Guard', 'Showroom grade paint protection ceramic details.', 'ic_teflon'), " +
                                "('Detailing', 'Alloy Buff & Polishing', 'Intense metallic shine brake dust repelling glaze finish.', 'ic_alloys'), " +
                                "('Interior Cleaning', 'Ozone Air Sterilization', 'Odour elimination, seat dry cleaning, vacuum finish.', 'ic_interior'), " +
                                "('Interior Cleaning', 'Dashboard Refurbishing', 'Bespoke UV shield protection coating.', 'ic_dashboard')")

                        db.execSQL("INSERT INTO reviews (reviewerName, rating, comment, vehicleModel, timestamp) VALUES " +
                                "('Abhishek Mishra', 5, ' Lucknow finally has a international standards futuristic automatic wash. Service done in 10 mins. Absolutely mirror finish! Fully satisfied.', 'Hyundai Creta', ${System.currentTimeMillis() - 86400000}), " +
                                "('Priya Sharma', 5, 'Amazing snow foam bath service. Staff is professional and machine wash does not scratch the metallic surface like manual rags. Loved the gloss.', 'Honda City', ${System.currentTimeMillis() - 172800000}), " +
                                "('Rohan Verma', 4, 'Excellent value for interior dry cleaning. The dashboard shines like brand new. Booking through app is very seamless - highly recommended!', 'Tata Nexon', ${System.currentTimeMillis() - 259200000}), " +
                                "('Vikram Kapoor', 5, 'Super luxury treatment. Applied Teflon coating, and my car looks dazzling. Best car wash center in UP. Smooth digital process.', 'BMW 3-Series', ${System.currentTimeMillis() - 345600000})")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
