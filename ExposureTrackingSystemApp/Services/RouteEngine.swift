import Foundation
import CoreLocation
import SwiftData

@MainActor
final class RouteEngine: ObservableObject {
    @Published var activeRoute: Route?
    @Published var currentDistanceMeters: Double = 0
    @Published var currentDurationSeconds: Double = 0
    @Published var currentAverageSpeedKmh: Double = 0

    private var modelContext: ModelContext?
    private var liveUpdateTimer: Timer?

    init() {
        self.activeRoute = nil
    }

    func configure(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func startNewRoute(name: String = "Route") {
        guard let modelContext else { return }

        let route = Route(name: name)
        modelContext.insert(route)
        activeRoute = route
        route.startedAt = Date()
        route.isActive = true
        route.updatedAt = Date()
        currentDistanceMeters = 0
        currentDurationSeconds = 0
        currentAverageSpeedKmh = 0
        startLiveTimer()
    }

    func appendLocation(_ location: CLLocation) {
        guard let modelContext, let route = activeRoute else { return }

        let point = LocationPoint(from: location, route: route)
        modelContext.insert(point)
        route.locationPoints.append(point)

        let points = route.locationPoints.sorted { $0.timestamp < $1.timestamp }

        if points.count > 1 {
            let previousPoint = points[points.count - 2]
            let segment = RouteSegment(start: previousPoint, end: point, route: route)
            modelContext.insert(segment)
            route.segments.append(segment)

            var distance = 0.0
            for index in 1..<points.count {
                let previous = points[index - 1]
                let current = points[index]
                distance += current.clLocation.distance(from: previous.clLocation)
            }

            route.totalDistanceMeters = distance
        }

        updateRouteMetrics(route: route)
    }

    func stopRoute() {
        guard let route = activeRoute else { return }
        route.endedAt = Date()
        route.isActive = false
        route.updatedAt = Date()
        updateRouteMetrics(route: route)
        stopLiveTimer()
        activeRoute = nil
    }

    private func startLiveTimer() {
        stopLiveTimer()
        liveUpdateTimer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            guard let self, let route = self.activeRoute else { return }
            self.updateRouteMetrics(route: route)
        }
    }

    private func stopLiveTimer() {
        liveUpdateTimer?.invalidate()
        liveUpdateTimer = nil
    }

    private func updateRouteMetrics(route: Route) {
        let elapsed = route.startedAt.map { Date().timeIntervalSince($0) } ?? 0
        route.totalDurationSeconds = max(elapsed, 0)
        route.averageSpeedKmh = route.totalDurationSeconds > 0
            ? (route.totalDistanceMeters / route.totalDurationSeconds) * 3.6
            : 0
        route.updatedAt = Date()

        currentDistanceMeters = route.totalDistanceMeters
        currentDurationSeconds = route.totalDurationSeconds
        currentAverageSpeedKmh = route.averageSpeedKmh
    }
}
