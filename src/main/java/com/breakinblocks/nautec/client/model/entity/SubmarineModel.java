package com.breakinblocks.nautec.client.model.entity;

import com.breakinblocks.nautec.Nautec;
import com.breakinblocks.nautec.content.entities.SubmarineEntity;
import com.geckolib.model.DefaultedEntityGeoModel;

public class SubmarineModel extends DefaultedEntityGeoModel<SubmarineEntity> {
    public SubmarineModel() {
        super(Nautec.rl("submarine"));
    }
}
