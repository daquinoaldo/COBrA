#!/usr/bin/env bash

# configs
contracts_path="contracts"
java_path="DAPP/contracts/src"
java_package="com.aldodaquino.cobra.contracts"

# prepare the output folder (deleting any previous compiled files)
output_path=${contracts_path}/out
rm -rf ${output_path}
mkdir -p ${output_path}

# for each file in the contracts_path
for file in ${contracts_path}/*.sol; do
    # get the contract name
    contract=${file//$contracts_path\//}
    contract=${contract//.sol/}
    echo ${contract}

    # compile files
    solc ${file} --bin --abi --optimize -o ${output_path}/${contract}

    # generate Java classes
    base_path=${output_path}/${contract}/${contract}
    web3j solidity generate ${base_path}.bin ${base_path}.abi -o ${java_path} -p ${java_package}
done