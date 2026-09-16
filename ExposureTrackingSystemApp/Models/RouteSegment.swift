import Foundation
import SwiftData
import CoreLocation

@Model
final class RouteSegment {
    @Attribute(.unique) var id: UUID
    @Relationship(inverse: \Route.segments)
    var route: Route?
    var startTime: Date
    var endTime: Date
    var distanceMeters: Double
    var durationSeconds: Double
    var averageSpeedKmh: Double
    var maxSpeedKmh: Double
    var startLatitude: Double
    var startLongitude: Double
    var endLatitude: Double
    var endLongitude: Double

    init(start: LocationPoint, end: LocationPoint, route: Route? = nil) {
        self.id = UUID()
        self.route = route
        self.startTime = start.timestamp
        self.endTime = end.timestamp
        self.startLatitude = start.latitude
        self.startLongitude = start.longitude
        self.endLatitude = end.latitude
        self.endLongitude = end.longitude

        let startLocation = start.clLocation
        let endLocation = end.clLocation
        let distance = endLocation.distance(from: startLocation)
        let duration = end.timestamp.timeIntervalSince(start.timestamp)

        self.distanceMeters = distance
        self.durationSeconds = duration
        self.averageSpeedKmh = duration > 0 ? (distance / duration) * 3.6 : 0
        self.maxSpeedKmh = max(start.speed, end.speed) * 3.6
    }
}
