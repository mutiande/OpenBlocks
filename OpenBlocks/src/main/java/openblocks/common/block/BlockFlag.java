package openblocks.common.block;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import openblocks.OpenBlocks.Blocks;
import openblocks.common.tileentity.TileEntityFlag;
import openmods.block.OpenBlock;
import openmods.colors.ColorMeta;
import openmods.geometry.Orientation;
import openmods.infobook.BookDocumentation;
import openmods.model.eval.EvalModelState;

@BookDocumentation(customName = "flag")
public class BlockFlag extends OpenBlock.SixDirections {

	@Nullable
	public static Block colorToBlock(ColorMeta color) {
		switch (color) {
			case BLACK:
				return Blocks.blackFlag;
			case BLUE:
				return Blocks.blueFlag;
			case BROWN:
				return Blocks.brownFlag;
			case CYAN:
				return Blocks.cyanFlag;
			case GRAY:
				return Blocks.grayFlag;
			case GREEN:
				return Blocks.greenFlag;
			case LIGHT_BLUE:
				return Blocks.lightBlueFlag;
			case LIGHT_GRAY:
				return Blocks.lightGrayFlag;
			case LIME:
				return Blocks.limeFlag;
			case MAGENTA:
				return Blocks.magentaFlag;
			case ORANGE:
				return Blocks.orangeFlag;
			case PINK:
				return Blocks.pinkFlag;
			case PURPLE:
				return Blocks.purpleFlag;
			case RED:
				return Blocks.redFlag;
			case WHITE:
				return Blocks.whiteFlag;
			case YELLOW:
				return Blocks.yellowFlag;
			default:
				return null;
		}
	}

	public BlockFlag() {
		super(Material.CIRCUITS);
		setPlacementMode(BlockPlacementMode.SURFACE);
		setHardness(0.0F);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this,
				new IProperty[] { getPropertyOrientation() },
				new IUnlistedProperty[] { EvalModelState.PROPERTY });
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	private static final AxisAlignedBB MIDDLE_AABB = new AxisAlignedBB(0.5 - (1.0 / 16.0), 0.0, 0.5 - (1.0 / 16.0), 0.5 + (1.0 / 16.0), 0.0 + 1.0, 0.5 + (1.0 / 16.0));
	private static final AxisAlignedBB NS_AABB = new AxisAlignedBB(0.5 - (1.0 / 16.0), 0.0, 0.5 - (5.0 / 16.0), 0.5 + (1.0 / 16.0), 0.0 + 1.0, 0.5 + (5.0 / 16.0));
	private static final AxisAlignedBB WE_AABB = new AxisAlignedBB(0.5 - (5.0 / 16.0), 0.0, 0.5 - (1.0 / 16.0), 0.5 + (5.0 / 16.0), 0.0 + 1.0, 0.5 + (1.0 / 16.0));

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		TileEntityFlag flag = getTileEntity(source, pos, TileEntityFlag.class);
		if (flag != null) {
			EnumFacing onSurface = flag.getOrientation().down();
			switch (onSurface) {
				case EAST:
				case WEST:
					return WE_AABB;
				case NORTH:
				case SOUTH:
					return NS_AABB;
				default:
					return MIDDLE_AABB;
			}
		}

		return MIDDLE_AABB;
	}

	private boolean isFlagOnGround(IBlockState state) {
		return state.getValue(getPropertyOrientation()).down() == EnumFacing.DOWN;
	}

	private boolean isBaseSolidForFlag(World world, BlockPos pos) {
		final IBlockState belowState = world.getBlockState(pos.down());
		final Block belowBlock = belowState.getBlock();
		if (belowBlock instanceof BlockFence) return true;
		if (belowBlock instanceof BlockFlag && isFlagOnGround(belowState)) return true;

		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.DOWN) return false;
		if (side == EnumFacing.UP && isBaseSolidForFlag(world, pos)) return true;

		return isNeighborBlockSolid(world, pos, side.getOpposite());
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos neigbour) {
		super.neighborChanged(state, world, pos, block, neigbour);

		final Orientation orientation = getOrientation(state);
		final EnumFacing down = orientation.down();

		if (isNeighborBlockSolid(world, pos, down)) return;
		if (isFlagOnGround(state) && isBaseSolidForFlag(world, pos)) return;

		world.destroyBlock(pos, true);
	}

	@Override
	public boolean canRotateWithTool() {
		return false;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		final TileEntityFlag te = getTileEntity(world, pos, TileEntityFlag.class);

		return (te != null)
				? ((IExtendedBlockState)state).withProperty(EvalModelState.PROPERTY, te.getRenderState())
				: state;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN? BlockFaceShape.MIDDLE_POLE_THIN : BlockFaceShape.UNDEFINED;
	}
}
