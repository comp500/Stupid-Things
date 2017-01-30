package furgl.stupidThings.common.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityReverseTntPrimed extends EntityTNTPrimed {

	public EntityReverseTntPrimed(World worldIn) {
		super(worldIn);
	}

	public EntityReverseTntPrimed(World worldIn, double x, double y, double z, EntityLivingBase igniter) {
		super(worldIn, x, y, z, igniter);
	}

	@Override
	public void onUpdate() {
		if (this.getFuse()-1 <= 0) {
			this.setDead();
			this.explode();
		}
		else
			super.onUpdate();
	}

	private void explode() {
		Explosion explosion = new ReverseExplosion(this.worldObj, this, this.posX, this.posY + (double)(this.height / 16.0F), this.posZ, 4.0F, false, true);
		if (!net.minecraftforge.event.ForgeEventFactory.onExplosionStart(this.worldObj, explosion)) {
			explosion.doExplosionA();
			explosion.doExplosionB(true);
		}
	}

	class ReverseExplosion extends Explosion {

		private Random explosionRNG;
		private ArrayList<BlockPos> affectedBlockPositions;
		private HashMap<EntityPlayer, Vec3d> playerKnockbackMap;
		private World worldObj;
		private Entity exploder;
		private float explosionSize;
		private double explosionX;
		private double explosionY;
		private double explosionZ;
		private boolean isFlaming;
		private boolean isSmoking;
		private Vec3d position;

		public ReverseExplosion(World world, Entity entity, double x, double y, double z, float size, boolean flaming, boolean smoking) {
			super(world, entity, x, y, z, size, flaming, smoking);
			this.explosionRNG = new Random();
			this.affectedBlockPositions = Lists.<BlockPos>newArrayList();
			this.playerKnockbackMap = Maps.<EntityPlayer, Vec3d>newHashMap();
			this.worldObj = world;
			this.exploder = entity;
			this.explosionSize = size;
			this.explosionX = x;
			this.explosionY = y;
			this.explosionZ = z;
			this.isFlaming = flaming;
			this.isSmoking = smoking;
			this.position = new Vec3d(explosionX, explosionY, explosionZ);
		}

		@Override
		/**Modified from Explosion#doExplosionA*/
		public void doExplosionA() {
			Set<BlockPos> set = Sets.<BlockPos>newHashSet();
			for (int j = 0; j < 16; ++j) {
				for (int k = 0; k < 16; ++k) {
					for (int l = 0; l < 16; ++l) {
						if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) {
							double d0 = (double)((float)j / 15.0F * 2.0F - 1.0F);
							double d1 = (double)((float)k / 15.0F * 2.0F - 1.0F);
							double d2 = (double)((float)l / 15.0F * 2.0F - 1.0F);
							double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
							d0 = d0 / d3;
							d1 = d1 / d3;
							d2 = d2 / d3;
							float f = this.explosionSize * (0.7F + this.worldObj.rand.nextFloat() * 0.6F);
							double d4 = this.explosionX;
							double d6 = this.explosionY;
							double d8 = this.explosionZ;

							for (; f > 0.0F; f -= 0.22500001F) {
								BlockPos blockpos = new BlockPos(d4, d6, d8);
								IBlockState iblockstate = this.worldObj.getBlockState(blockpos);

								if (iblockstate.getMaterial() != Material.AIR) {
									float f2 = this.exploder != null ? this.exploder.getExplosionResistance(this, this.worldObj, blockpos, iblockstate) : iblockstate.getBlock().getExplosionResistance(worldObj, blockpos, (Entity)null, this);
									f -= (f2 + 0.3F) * 0.3F;
								}

								if (f > 0.0F && (this.exploder == null || this.exploder.verifyExplosion(this, this.worldObj, blockpos, iblockstate, f))) 
									set.add(blockpos);

								d4 += d0 * 0.30000001192092896D;
								d6 += d1 * 0.30000001192092896D;
								d8 += d2 * 0.30000001192092896D;
							}
						}
					}
				}
			}

			this.affectedBlockPositions.addAll(set);
			float f3 = this.explosionSize * 5.0F; //orig: * 2.0f
			int k1 = MathHelper.floor_double(this.explosionX - (double)f3 - 1.0D);
			int l1 = MathHelper.floor_double(this.explosionX + (double)f3 + 1.0D);
			int i2 = MathHelper.floor_double(this.explosionY - (double)f3 - 1.0D);
			int i1 = MathHelper.floor_double(this.explosionY + (double)f3 + 1.0D);
			int j2 = MathHelper.floor_double(this.explosionZ - (double)f3 - 1.0D);
			int j1 = MathHelper.floor_double(this.explosionZ + (double)f3 + 1.0D);
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this.exploder, new AxisAlignedBB((double)k1, (double)i2, (double)j2, (double)l1, (double)i1, (double)j1));
			net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.worldObj, this, list, f3);
			Vec3d vec3d = new Vec3d(this.explosionX, this.explosionY, this.explosionZ);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				Entity entity = (Entity)list.get(k2);

				if (!entity.isImmuneToExplosions()) {
					double d12 = entity.getDistance(this.explosionX, this.explosionY, this.explosionZ) / (double)f3;

					if (d12 <= 1.0D) {
						double d5 = entity.posX - this.explosionX;
						double d7 = entity.posY + (double)entity.getEyeHeight() - this.explosionY;
						double d9 = entity.posZ - this.explosionZ;
						double d13 = (double)MathHelper.sqrt_double(d5 * d5 + d7 * d7 + d9 * d9);

						if (d13 != 0.0D) {
							d5 = d5 / d13;
							d7 = d7 / d13;
							d9 = d9 / d13;
							double d14 = (double)this.worldObj.getBlockDensity(vec3d, entity.getEntityBoundingBox());
							double d10 = (1.0D - d12) * d14;
							//entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f3 + 1.0D)));
							double d11 = 1.0D;

							if (entity instanceof EntityLivingBase)
								d11 = EnchantmentProtection.getBlastDamageReduction((EntityLivingBase)entity, d10);

							//added to reverse velocity
							d10 *= -3;
							d11 *= -3;

							entity.motionX += d5 * d11;
							entity.motionY += d7 * d11;
							entity.motionZ += d9 * d11;

							if (entity instanceof EntityPlayer) {
								EntityPlayer entityplayer = (EntityPlayer)entity;

								if (!entityplayer.isSpectator() && (!entityplayer.isCreative() || !entityplayer.capabilities.isFlying))
									this.playerKnockbackMap.put(entityplayer, new Vec3d(d5 * d10, d7 * d10, d9 * d10));
							}
						}
					}
				}
			}
		}

		@Override
		/**Modified from Explosion#doExplosionB*/
		public void doExplosionB(boolean spawnParticles) {
			if (!this.worldObj.isRemote) {
				this.worldObj.playSound((EntityPlayer)null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.BLOCKS, 4.0F, this.worldObj.rand.nextFloat());
				this.worldObj.playSound((EntityPlayer)null, this.explosionX, this.explosionY, this.explosionZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
			}
			else {
				if (this.explosionSize >= 2.0F && this.isSmoking)
					this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D, new int[0]);
				else
					this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D, 0.0D, new int[0]);
			}

			if (this.isSmoking) {
				for (BlockPos blockpos : this.affectedBlockPositions) {
					//IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
					//Block block = iblockstate.getBlock();

					if (spawnParticles && this.worldObj.isRemote) {
						double d0 = (double)((float)blockpos.getX() + this.worldObj.rand.nextFloat());
						double d1 = (double)((float)blockpos.getY() + this.worldObj.rand.nextFloat());
						double d2 = (double)((float)blockpos.getZ() + this.worldObj.rand.nextFloat());
						double d3 = d0 - this.explosionX;
						double d4 = d1 - this.explosionY;
						double d5 = d2 - this.explosionZ;
						double d6 = (double)MathHelper.sqrt_double(d3 * d3 + d4 * d4 + d5 * d5);
						d3 = d3 / d6;
						d4 = d4 / d6;
						d5 = d5 / d6;
						double d7 = 0.5D / (d6 / (double)this.explosionSize + 0.1D);
						d7 = d7 * (double)(this.worldObj.rand.nextFloat() * this.worldObj.rand.nextFloat() + 0.3F);
						d3 = d3 * d7;
						d4 = d4 * d7;
						d5 = d5 * d7;

						//added to reverse particle direction
						d3 *= -1.5d;
						d4 *= -1.5d;
						d5 *= -1.5d;

						this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (d0 + this.explosionX) / 2.0D, (d1 + this.explosionY) / 2.0D, (d2 + this.explosionZ) / 2.0D, d3, d4, d5, new int[0]);
						this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
						this.worldObj.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, -d3*8, -d4*8, -d5*8, new int[0]);
					}

					/*if (iblockstate.getMaterial() != Material.AIR && !this.worldObj.isRemote) {
						if (block.canDropFromExplosion(this))
							block.dropBlockAsItemWithChance(this.worldObj, blockpos, this.worldObj.getBlockState(blockpos), 1.0F / this.explosionSize, 0);

						block.onBlockExploded(this.worldObj, blockpos, this);
					}*/
				}

				//move blocks to this position (in sphere)
				if (!this.worldObj.isRemote) {
					int movedBlocks = 0;
					for (int radius=0; radius<=this.explosionSize; radius++) 
						for (int y=-radius; y<=radius; y++)
							for (int x=-radius+Math.abs(y); x<=radius-Math.abs(y); x++)
								for (int z=-radius+Math.abs(y); z<=radius-Math.abs(y); z++)
									if (!(z == radius && x == radius) && this.worldObj.isAirBlock(new BlockPos(this.position.addVector(x, y, z)))) 
										for (; movedBlocks<this.affectedBlockPositions.size(); movedBlocks++) 
											if (this.worldObj.getBlockState(this.affectedBlockPositions.get(movedBlocks)).getMaterial() != Material.AIR) {
												IBlockState state = this.worldObj.getBlockState(this.affectedBlockPositions.get(movedBlocks));
												this.worldObj.setBlockToAir(this.affectedBlockPositions.get(movedBlocks));
												this.worldObj.setBlockState(new BlockPos(this.position.addVector(x, y, z)), state);
												if (state.getBlock() instanceof BlockTNT)
													state.getBlock().onBlockExploded(worldObj, new BlockPos(this.position.addVector(x, y, z)), this);
												movedBlocks++;
												break;
											}
				}
			}

			if (this.isFlaming && !this.worldObj.isRemote) 
				for (BlockPos blockpos1 : this.affectedBlockPositions)
					if (this.worldObj.getBlockState(blockpos1).getMaterial() == Material.AIR && this.worldObj.getBlockState(blockpos1.down()).isFullBlock() && this.explosionRNG.nextInt(3) == 0)
						this.worldObj.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
		}
	}
}