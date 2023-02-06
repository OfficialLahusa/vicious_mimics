package com.lahusa.vicious_mimics.entity.ai.goal;

import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.TameableEntity;

public class MimicSitGoal extends SitGoal {
    protected final TameableEntity tameableMirror;

    public MimicSitGoal(TameableEntity tameable) {
        super(tameable);
        tameableMirror = tameable;
    }

    @Override
    public boolean canStart() {
        if(tameableMirror.isSitting()) return true;
        return super.canStart();
    }
}
