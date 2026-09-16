import Foundation
import SwiftData

@Model
final class Route {
    @Attribute(.unique) var id: UUID
    var name: String
    var createdAt: Date
    var updatedAt: Date
    var startedAt: Date?
    var endedAt: Date?
    var totalDistanceMeters: Double
    var totalDurationSeconds: Double
    var averageSpeedKmh: Double
    var isActive: Bool

    @Relationship(deleteRule: .cascade, inverse: \LocationPoint.route)
    var locationPoints: [LocationPoint] = []

    @Relationship(deleteRule: .cascade, inverse: \RouteSegment.route)
    var segments: [RouteSegment] = []

    init(name: String = "Route") {
        self.id = UUID()
        self.name = name
        let now = Date()
        self.createdAt = now
        self.updatedAt = now
        self.startedAt = now
        self.endedAt = nil
        self.totalDistanceMeters = 0
        self.totalDurationSeconds = 0
        self.averageSpeedKmh = 0
        self.isActive = true
    }
}
