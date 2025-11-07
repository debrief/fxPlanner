package com.planetmayo.usvsim.api.service;

import com.planetmayo.usvsim.api.dto.*;
import com.planetmayo.usvsim.controller.BehaviourExecutor;
import com.planetmayo.usvsim.model.behaviour.*;
import com.planetmayo.usvsim.model.geometry.Position;
import com.planetmayo.usvsim.model.platform.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Stateless simulation service for single tick execution.
 *
 * Architecture:
 * - Client sends current state (platform + behavior)
 * - Server computes next state using refactored stateless behaviors
 * - Returns updated state (no server-side storage)
 *
 * This enables:
 * - Horizontal scaling (any server can handle any request)
 * - Client-side state management (React)
 * - Simple deployment (no session persistence)
 */
@Service
public class SimulationService {

    /**
     * Execute single simulation tick.
     *
     * @param request contains platform state, behavior state, deltaTime
     * @return updated platform state, behavior state, demand, completion flags
     */
    public SimulationTickResponse tick(SimulationTickRequest request) {
        // Convert DTOs to domain models
        PlatformState platformState = toPlatformState(request.platformState());
        BehaviourExecutionState behaviorState = toBehaviourExecutionState(request.behaviorState());

        // TODO: Deserialize behavior from config (for now, throw error)
        if (request.behaviorConfig() != null && !request.behaviorConfig().isBlank()) {
            throw new UnsupportedOperationException(
                "Behavior config deserialization not yet implemented. " +
                "For now, behaviors must be pre-created on client side."
            );
        }

        // For MVP, we'll require behaviors to be sent as full state from client
        // This is a placeholder - real implementation needs behavior deserialization
        throw new UnsupportedOperationException(
            "Simulation tick requires behavior instance. " +
            "This will be implemented when behavior serialization is added in Phase 7 (T039)."
        );

        // TODO: Implement actual tick logic when behavior serialization is ready:
        // 1. Get behavior instance from config or client-provided full state
        // 2. Call behavior.calculateDemand(behaviorState, platformState)
        // 3. Apply dynamics to compute new platform state
        // 4. Call behavior.updateProgress(behaviorState, newPlatformState)
        // 5. Check behavior.isComplete(newBehaviorState)
        // 6. Convert to DTOs and return
    }

    /**
     * Convert DTO to domain PlatformState.
     */
    private PlatformState toPlatformState(PlatformStateDTO dto) {
        return new PlatformState(
            dto.platformId(),
            Position.of(dto.position().latitude(), dto.position().longitude()),
            dto.heading(),
            dto.speed(),
            dto.depth(),
            Instant.parse(dto.timestamp())
        );
    }

    /**
     * Convert DTO to domain BehaviourExecutionState.
     */
    private BehaviourExecutionState toBehaviourExecutionState(BehaviorExecutionStateDTO dto) {
        return new BehaviourExecutionState(
            dto.behaviorId(),
            dto.currentWaypointIndex(),
            BehaviourState.valueOf(dto.state()),
            dto.lastDistanceToWaypoint()
        );
    }

    /**
     * Convert domain PlatformState to DTO.
     */
    private PlatformStateDTO toPlatformStateDTO(PlatformState state) {
        return new PlatformStateDTO(
            state.getId(),
            new PositionDTO(state.getPosition().getLatitude(), state.getPosition().getLongitude()),
            state.getHeading(),
            state.getSpeed(),
            state.getDepth(),
            state.getTimestamp().toString()
        );
    }

    /**
     * Convert domain BehaviourExecutionState to DTO.
     */
    private BehaviorExecutionStateDTO toBehaviorExecutionStateDTO(BehaviourExecutionState state) {
        return new BehaviorExecutionStateDTO(
            state.behaviorId(),
            state.currentWaypointIndex(),
            state.state().name(),
            state.lastDistanceToWaypoint()
        );
    }

    /**
     * Convert domain PlatformDemand to DTO.
     */
    private PlatformDemandDTO toPlatformDemandDTO(PlatformDemand demand) {
        return new PlatformDemandDTO(
            demand.getDemandedHeading(),
            demand.getDemandedSpeed(),
            demand.getDemandedDepth(),
            demand.getTurnDirection().name()
        );
    }
}
