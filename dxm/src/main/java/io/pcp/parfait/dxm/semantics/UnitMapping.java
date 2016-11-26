package io.pcp.parfait.dxm.semantics;

import static java.lang.String.format;
import static systems.uom.unicode.CLDR.BIT;
import static systems.uom.unicode.CLDR.BYTE;
import static tec.units.ri.AbstractConverter.IDENTITY;
import static tec.units.ri.AbstractQuantity.NONE;
import static tec.units.ri.AbstractUnit.ONE;
import static tec.units.ri.unit.Units.SECOND;

import javax.measure.quantity.Dimensionless;
import javax.measure.Unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.pcp.parfait.dxm.semantics.PcpScale.SpaceScale;
import io.pcp.parfait.dxm.semantics.PcpScale.TimeScale;
import io.pcp.parfait.dxm.semantics.PcpScale.UnitScale;

public final class UnitMapping {
    private static final Logger LOG = LoggerFactory.getLogger(UnitMapping.class);

    private final Unit<?> unit;
    private final PcpDimensionSet dimensionSet;
    private final PcpScale<?> unitDimension;
    private final PcpScale<?> perDimension;

    UnitMapping(Unit<?> unit, PcpDimensionSet dimensionSet, PcpScale<?> unitDimension,
            PcpScale<?> perDimension) {
        this.unit = unit;
        this.dimensionSet = dimensionSet;
        this.unitDimension = unitDimension;
        this.perDimension = perDimension;
    }

    @Override
    public String toString() {
        return "Mapping(unit=" + unit + " → " + dimensionSet + "=" + unitDimension
                + (perDimension == null ? "" : " per " + perDimension) + ")";
    }

    public static UnitMapping findUnitMapping(Unit<?> unit) {
        UnitMapping approximateMatch = null;

        for (PcpDimensionSet dimensionSet : PcpDimensionSet.values()) {
            for (UnitMapping mapping : dimensionSet.getUnitMappings()) {
                if (unit.equals(mapping.unit)) {
                    return mapping;
                }
                if (approximateMatch == null && areFunctionallyIdenticalUnits(unit, mapping.unit)) {
                    approximateMatch = mapping;
                }
            }
        }
        return approximateMatch;
    }

    private static boolean areFunctionallyIdenticalUnits(Unit<?> left, Unit<?> right) {
        if (!left.isCompatible(right)) {
            return false;
        }
        Unit<?> divided = left.divide(right);
        if (!divided.getDimension().equals(NONE)) {
            return false;
        }
        return divided.asType(Dimensionless.class).getConverterTo(ONE).equals(IDENTITY);
    }

    public static int getDimensions(Unit<?> unit, String name) {
        System.out.println("XXX: UNIT IS " + unit);
        if (unit == null) {
            return 0;
        }

        Dimension spaceDimension = Dimension.NONE;
        Dimension timeDimension = Dimension.NONE;
        Dimension unitDimension = Dimension.UNITS;
        PcpScale<?> spaceScale = null;
        PcpScale<?> timeScale = null;
        PcpScale<?> unitScale = UnitScale.UNIT;

        UnitMapping mapping = findUnitMapping(unit);
        System.out.println("XXX: UNITMAPPING IS " + mapping);

        if (mapping == null) {
            LOG.warn("No mapping found for unit " + unit + " of metric " + name
                    + "; treating as a unit quantity");
        } else {
            LOG.debug("Found mapping " + mapping + " for metric " + name);
            spaceDimension = mapping.getDimension(SpaceScale.class);
            timeDimension = mapping.getDimension(TimeScale.class);
            unitDimension = mapping.getDimension(UnitScale.class);
            spaceScale = mapping.getScale(SpaceScale.class);
            timeScale = mapping.getScale(TimeScale.class);
            unitScale = mapping.getScale(UnitScale.class);
        }
        System.out.println(format("XXX: unitScale:%s, timeScale:%s, spaceScale:%s, unitDimension:%s, timeDimension:%s, spaceDimension%s",
                unitScale, timeScale, spaceScale, unitDimension, timeDimension, spaceDimension));
        return assembleDimensions(unitScale, timeScale, spaceScale, unitDimension, timeDimension,
                spaceDimension);
    }

    private PcpScale<?> getScale(Class<? extends PcpScale<?>> scaleClass) {
        if (perDimension != null && scaleClass.equals(perDimension.getClass())) {
            return perDimension;
        } else if (scaleClass.equals(unitDimension.getClass())) {
            return unitDimension;
        }
        return null;
    }

    private Dimension getDimension(Class<? extends PcpScale<?>> scaleClass) {
        if (perDimension != null && scaleClass.equals(perDimension.getClass())) {
            return Dimension.PER;
        } else if (scaleClass.equals(unitDimension.getClass())) {
            return Dimension.UNITS;
        }
        return Dimension.NONE;
    }

    private static int assembleDimensions(PcpScale<?> unitScale, PcpScale<?> timeScale,
            PcpScale<?> spaceScale, Dimension unitDimension, Dimension timeDimension,
            Dimension spaceDimension) {
        int result = 0;
        result |= to4Bits(unitScale) << 8;
        result |= to4Bits(timeScale) << 12;
        result |= to4Bits(spaceScale) << 16;
        result |= to4Bits(unitDimension) << 20;
        result |= to4Bits(timeDimension) << 24;
        result |= to4Bits(spaceDimension) << 28;
        return result;
    }

    private static int to4Bits(UnitValued valued) {
        return valued == null ? 0 : (valued.getPmUnitsValue() & 0xf);
    }

    public static void main(String[] args) {
        System.out.println(UnitMapping.findUnitMapping(BYTE));
        System.out.println(UnitMapping.findUnitMapping(BYTE.divide(SECOND)));
        System.out.println(UnitMapping.findUnitMapping(BYTE.multiply(1024).divide(SECOND)));
        System.out.println(UnitMapping.findUnitMapping(BYTE.multiply(1024).divide(SECOND.divide(1000))));
        System.out.println(UnitMapping.findUnitMapping(ONE.multiply(1000).divide(SECOND)));
        System.out.println(UnitMapping.findUnitMapping(ONE.multiply(1000).divide(SECOND.divide(1000))));
        System.out.println(UnitMapping.findUnitMapping(ONE.multiply(500).divide(SECOND.divide(2))));
        System.out.println(UnitMapping.findUnitMapping(ONE.multiply(50).divide(BIT)));
    }
}
