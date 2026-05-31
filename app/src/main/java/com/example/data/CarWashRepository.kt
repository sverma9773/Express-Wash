package com.example.data

import kotlinx.coroutines.flow.Flow

class CarWashRepository(private val dao: CarWashDao) {

    // Bookings operations
    val allBookings: Flow<List<Booking>> = dao.getAllBookings()

    fun searchBookings(mobile: String): Flow<List<Booking>> {
        return dao.searchBookingsByMobile(mobile)
    }

    suspend fun bookAppointment(booking: Booking) {
        dao.insertBooking(booking)
    }

    suspend fun updateBookingStatus(id: Int, status: String) {
        dao.updateBookingStatus(id, status)
    }

    suspend fun cancelBooking(id: Int) {
        dao.updateBookingStatus(id, "Cancelled")
    }

    suspend fun deleteBooking(id: Int) {
        dao.deleteBooking(id)
    }

    // Offers operations
    val allOffers: Flow<List<Offer>> = dao.getAllOffers()

    suspend fun addOffer(offer: Offer) {
        dao.insertOffer(offer)
    }

    suspend fun removeOffer(id: Int) {
        dao.deleteOffer(id)
    }

    // Gallery operations
    val allGalleryItems: Flow<List<GalleryItem>> = dao.getAllGalleryItems()

    suspend fun addGalleryItem(item: GalleryItem) {
        dao.insertGalleryItem(item)
    }

    suspend fun removeGalleryItem(id: Int) {
        dao.deleteGalleryItem(id)
    }

    // Reviews operations
    val allReviews: Flow<List<Review>> = dao.getAllReviews()

    suspend fun addReview(review: Review) {
        dao.insertReview(review)
    }
}
