from mpi4py import MPI
import numpy as np

comm = MPI.COMM_WORLD
rank = comm.Get_rank()
size = comm.Get_size()

# Only rank 0 handles input
if rank == 0:
    ARRAY_SIZE = int(input("Enter the size of the array: "))
    print(f"Enter {ARRAY_SIZE} integer elements:")
    array = np.array([int(input()) for _ in range(ARRAY_SIZE)], dtype='i')

    # Pad the array if not divisible by size
    if ARRAY_SIZE % size != 0:
        padding = size - (ARRAY_SIZE % size)
        array = np.append(array, [0]*padding)
        ARRAY_SIZE = ARRAY_SIZE + padding
else:
    ARRAY_SIZE = None
    array = None

# Broadcast new ARRAY_SIZE after padding
ARRAY_SIZE = comm.bcast(ARRAY_SIZE, root=0)

# Create local subarray
subarray_size = ARRAY_SIZE // size
subarray = np.empty(subarray_size, dtype='i')

# Scatter array among all processes
comm.Scatter([array, MPI.INT], [subarray, MPI.INT], root=0)

# Compute local sum
local_sum = np.sum(subarray)
print(f"Process {rank} local sum is {local_sum}")

# Reduce to get total sum
sum_result = comm.reduce(local_sum, op=MPI.SUM, root=0)

if rank == 0:
    print(f"The sum of the elements is {sum_result}")
