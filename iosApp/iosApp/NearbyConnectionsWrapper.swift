import Foundation
import NearbyConnections
import EasyLocalGameKmpLibraryKit

/**
 * Swift implementation of NearbyConnectionsBridge for iOS.
 *
 * This class bridges the Kotlin NearbyConnectionsManager to the
 * Google NearbyConnections iOS SDK.
 *
 * Usage:
 * 1. Add NearbyConnections SDK to your project via CocoaPods or SPM
 * 2. Create an instance of this wrapper
 * 3. Call setSwiftBridge() on the Kotlin NearbyConnectionsManager
 *
 * CocoaPods:
 * pod 'NearbyConnections'
 *
 * SPM:
 * https://github.com/nicolo-cavalli/NearbyConnections
 */
class NearbyConnectionsWrapper: NearbyConnectionsBridge {
    
    private var connectionManager: GNCConnectionManager?
    private var advertiser: GNCAdvertiser?
    private var discoverer: GNCDiscoverer?
    
    private var connectionCallbacks: ConnectionCallbacks?
    private var payloadCallbacks: PayloadCallbacks?
    private var discoveryCallbacks: DiscoveryCallbacks?
    
    private var connectedEndpoints: Set<String> = []
    
    // MARK: - NearbyConnectionsBridge Implementation
    
    func startAdvertising(
        name: String,
        serviceId: String,
        strategy: ConnectionStrategy,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: @escaping () -> Void,
        onFailure: @escaping (KotlinException) -> Void
    ) {
        self.connectionCallbacks = connectionCallbacks
        self.payloadCallbacks = payloadCallbacks
        
        // Initialize connection manager
        connectionManager = GNCConnectionManager(
            serviceID: serviceId,
            strategy: mapStrategy(strategy)
        )
        connectionManager?.delegate = self
        
        // Initialize advertiser
        advertiser = GNCAdvertiser(connectionManager: connectionManager!)
        advertiser?.delegate = self
        
        // Start advertising
        guard let endpointInfo = name.data(using: .utf8) else {
            onFailure(KotlinException(message: "Failed to encode endpoint name"))
            return
        }
        
        advertiser?.startAdvertising(using: endpointInfo)
        onSuccess()
    }
    
    func stopAdvertising() {
        advertiser?.stopAdvertising()
        advertiser = nil
    }
    
    func startDiscovery(
        serviceId: String,
        strategy: ConnectionStrategy,
        discoveryCallbacks: DiscoveryCallbacks,
        onSuccess: @escaping () -> Void,
        onFailure: @escaping (KotlinException) -> Void
    ) {
        self.discoveryCallbacks = discoveryCallbacks
        
        // Initialize connection manager if not already done
        if connectionManager == nil {
            connectionManager = GNCConnectionManager(
                serviceID: serviceId,
                strategy: mapStrategy(strategy)
            )
            connectionManager?.delegate = self
        }
        
        // Initialize discoverer
        discoverer = GNCDiscoverer(connectionManager: connectionManager!)
        discoverer?.delegate = self
        
        discoverer?.startDiscovery()
        onSuccess()
    }
    
    func stopDiscovery() {
        discoverer?.stopDiscovery()
        discoverer = nil
    }
    
    func requestConnection(
        name: String,
        endpointId: String,
        connectionCallbacks: ConnectionCallbacks,
        payloadCallbacks: PayloadCallbacks,
        onSuccess: @escaping () -> Void,
        onFailure: @escaping (KotlinException) -> Void
    ) {
        self.connectionCallbacks = connectionCallbacks
        self.payloadCallbacks = payloadCallbacks
        
        guard let endpointInfo = name.data(using: .utf8) else {
            onFailure(KotlinException(message: "Failed to encode endpoint name"))
            return
        }
        
        discoverer?.requestConnection(to: endpointId, using: endpointInfo)
        onSuccess()
    }
    
    func acceptConnection(endpointId: String, payloadCallbacks: PayloadCallbacks) {
        self.payloadCallbacks = payloadCallbacks
        // Connection acceptance is handled in delegate methods
        // The verificationHandler(true) call in connectionManager delegate handles this
    }
    
    func rejectConnection(endpointId: String) {
        // Connection rejection handled via verificationHandler(false)
        // This would need to be called from the delegate
    }
    
    func sendPayload(endpointId: String, payload: KotlinByteArray) {
        let data = Data(payload.toNSData())
        connectionManager?.send(data, to: [endpointId])
    }
    
    func sendPayload(endpointIds: [String], payload: KotlinByteArray) {
        let data = Data(payload.toNSData())
        connectionManager?.send(data, to: endpointIds)
    }
    
    func disconnectFromEndpoint(endpointId: String) {
        connectionManager?.disconnect(from: endpointId)
        connectedEndpoints.remove(endpointId)
    }
    
    func stopAllEndpoints() {
        for endpoint in connectedEndpoints {
            connectionManager?.disconnect(from: endpoint)
        }
        connectedEndpoints.removeAll()
        stopAdvertising()
        stopDiscovery()
    }
    
    // MARK: - Helper Methods
    
    private func mapStrategy(_ strategy: ConnectionStrategy) -> GNCStrategy {
        switch strategy {
        case .p2pPointToPoint:
            return .pointToPoint
        case .p2pStar:
            return .star
        case .p2pCluster:
            return .cluster
        default:
            return .star
        }
    }
}

// MARK: - GNCConnectionManagerDelegate

extension NearbyConnectionsWrapper: GNCConnectionManagerDelegate {
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didReceive verificationCode: String,
        from endpointID: String,
        verificationHandler: @escaping (Bool) -> Void
    ) {
        // Auto-accept connections (can be customized)
        connectionCallbacks?.onConnectionInitiated(endpointId: endpointID, endpointName: "")
        verificationHandler(true)
    }
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didReceive data: Data,
        withID payloadID: Int64,
        from endpointID: String
    ) {
        let byteArray = data.toKotlinByteArray()
        payloadCallbacks?.onPayloadReceived(endpointId: endpointID, payload: byteArray)
    }
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didReceive stream: InputStream,
        withID payloadID: Int64,
        from endpointID: String,
        cancellationToken token: GNCCancellationToken
    ) {
        // Stream payloads not directly supported in this implementation
    }
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didStartReceivingResourceWithID payloadID: Int64,
        from endpointID: String,
        at localURL: URL,
        withName name: String,
        cancellationToken token: GNCCancellationToken
    ) {
        // File payloads not directly supported in this implementation
    }
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didReceiveTransferUpdate update: GNCTransferUpdate,
        from endpointID: String,
        forPayload payloadID: Int64
    ) {
        let isSuccess: KotlinBoolean? = {
            switch update {
            case .success:
                return KotlinBoolean(bool: true)
            case .failure, .canceled:
                return KotlinBoolean(bool: false)
            case .inProgress(let progress):
                return nil
            @unknown default:
                return nil
            }
        }()
        
        payloadCallbacks?.onPayloadTransferUpdate(
            endpointId: endpointID,
            payloadId: payloadID,
            bytesTransferred: 0,
            totalBytes: 0,
            isSuccess: isSuccess
        )
    }
    
    func connectionManager(
        _ connectionManager: GNCConnectionManager,
        didChangeTo state: GNCConnectionState,
        for endpointID: String
    ) {
        switch state {
        case .connecting:
            // Connection in progress
            break
        case .connected:
            connectedEndpoints.insert(endpointID)
            connectionCallbacks?.onConnectionResult(
                endpointId: endpointID,
                isSuccess: true,
                statusCode: Int32(ConnectionStatusCodes.companion.STATUS_OK)
            )
        case .disconnected:
            connectedEndpoints.remove(endpointID)
            connectionCallbacks?.onDisconnected(endpointId: endpointID)
        case .rejected:
            connectionCallbacks?.onConnectionResult(
                endpointId: endpointID,
                isSuccess: false,
                statusCode: Int32(ConnectionStatusCodes.companion.STATUS_CONNECTION_REJECTED)
            )
        @unknown default:
            break
        }
    }
}

// MARK: - GNCAdvertiserDelegate

extension NearbyConnectionsWrapper: GNCAdvertiserDelegate {
    
    func advertiser(
        _ advertiser: GNCAdvertiser,
        didReceiveConnectionRequestFrom endpointID: String,
        with context: Data,
        connectionRequestHandler: @escaping (Bool) -> Void
    ) {
        // Auto-accept connection requests
        connectionCallbacks?.onConnectionInitiated(
            endpointId: endpointID,
            endpointName: String(data: context, encoding: .utf8) ?? ""
        )
        connectionRequestHandler(true)
    }
}

// MARK: - GNCDiscovererDelegate

extension NearbyConnectionsWrapper: GNCDiscovererDelegate {
    
    func discoverer(
        _ discoverer: GNCDiscoverer,
        didFind endpointID: String,
        with context: Data
    ) {
        let endpointName = String(data: context, encoding: .utf8) ?? ""
        discoveryCallbacks?.onEndpointFound(
            endpointId: endpointID,
            endpointName: endpointName,
            serviceId: ""
        )
    }
    
    func discoverer(_ discoverer: GNCDiscoverer, didLose endpointID: String) {
        discoveryCallbacks?.onEndpointLost(endpointId: endpointID)
    }
}

// MARK: - Data Extensions

extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let count = self.count
        let byteArray = KotlinByteArray(size: Int32(count))
        for (index, byte) in self.enumerated() {
            byteArray.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return byteArray
    }
}

extension KotlinByteArray {
    func toNSData() -> Data {
        var data = Data(count: Int(self.size))
        for i in 0..<self.size {
            data[Int(i)] = UInt8(bitPattern: self.get(index: i))
        }
        return data
    }
}
