    
// Load the AWS SDK for Node.js
var AWS = require('aws-sdk');
// Set the region 
AWS.config.update({region: 'eu-central-1'});

// Create the DynamoDB service object
var ddb = new AWS.DynamoDB.DocumentClient({apiVersion: '2012-08-10'});
let tableName = "buildings";


exports.handler = async (event) => {
    let body = event;
    let buildingId = body.buildingId;
    let thingId = body.thingId;
    let shadows = body.shadows;
    
    try {
        let shadowsToUpdate = await initializeShadows(buildingId, thingId, shadows);
        console.log("shadows To update")
        //console.log(shadowsToUpdate)
        let updateResult = await updateRemainingShadows(buildingId, thingId, shadowsToUpdate);
        
        if (updateResult) {
            console.log("successful update");
            //console.log(updateResult);
            const response = {
                statusCode: 200,
                body: JSON.stringify({
                    message: "Updated DynamoDB with new devices",
                    data: updateResult
                }),
            };
            return response;
        }
    } catch (error) {
        console.log(error);
    }
};

async function updateRemainingShadows(buildingId, thingId, shadowsLeft) {
    let params = createDynamoDBParams(buildingId, thingId, shadowsLeft);
    if (params) return await ddb.update(params).promise();
    else return false
   
}

function createDynamoDBParams(buildingId, thingId, shadows) {
    let sets = [];
    let attributeNames = { "#thingId": thingId };
    let attributeValues = {};
    let start = "things.#thingId.devices.";

    var shadowIndex = 0;
    var propertyIndex = 0;
    
    for (const [shadowId, shadow] of Object.entries(shadows)) {
        attributeNames[`#shadow_${shadowIndex}`] = shadowId;
        let exposesVal = `:exposes_${shadowIndex}`;
        let exposesSet = start + `#shadow_${shadowIndex}.exposes = ${exposesVal}`;
        sets.push(exposesSet);
        attributeValues[`${exposesVal}`] = shadow.exposes;
      
        for (const[propertyId, property] of Object.entries(shadow.properties)) {
           let propertyVar = `#pVar_${propertyIndex}`;
           let propertyValue = `:pVal_${propertyIndex}`;
           let propertySet = start + `#shadow_${shadowIndex}.${propertyVar} = ${propertyValue}`;
           sets.push(propertySet);
           attributeNames[`${propertyVar}`] = propertyId;
           attributeValues[`${propertyValue}`] = property;
           propertyIndex++;
        }
        shadowIndex++;
    }
    if (Object.keys(attributeValues).length === 0) {
        return false;
    }
    console.log("updating existing shadows")
    //console.log(sets)
    //console.log(attributeNames)
    //console.log(attributeValues)
    return {
        TableName: tableName,
        Key:{ "id": buildingId },  
        UpdateExpression: "set " + sets.join(", "),  
        ExpressionAttributeNames: attributeNames,
        ExpressionAttributeValues: attributeValues,
        ReturnValues:"UPDATED_NEW"
    };
}


async function initializeShadows(buildingId, thingId, shadows) {
    var params = {
        TableName: tableName,
        Key:{ "id": buildingId }
    };
 
    let building = await ddb.get(params).promise();
    if (building) {
        if (!building.Item.things[thingId]) {
            console.log("thing not found, initializing");
            let params = {
                TableName: tableName,
                Key: { 'id' : buildingId },
                UpdateExpression: "set things.#thingName = :thingValue",
                ExpressionAttributeNames : { "#thingName": thingId },
                ExpressionAttributeValues: { ':thingValue' : { devices: shadowsToDynamoMap(shadows) } }
            };
            let updateResult = await ddb.update(params).promise();
            if (updateResult) {
                console.log("initialized the whole thing");
                //console.log(updateResult);
            }
            return false;
        }
        
        let sh = divideShadows(shadows, building.Item.things[thingId].devices);
        let shadowsToCreate = sh.shadowsToCreate;
        console.log("shadowsToCreate")
        console.log(shadowsToCreate)
        let params = initializationParamsDynamoDB(buildingId, thingId, shadowsToDynamoMap(shadowsToCreate));
        if (params) {
            console.log("creating stuff")
            console.log(params)
            let res = await ddb.update(params).promise();
            if (res) {
                console.log("created missing shadows");
                return sh.shadowsToUpdate;
            }
        }
        return sh.shadowsToUpdate;
    }
}

function initializationParamsDynamoDB(buildingId, thingName, shadows) {
    let sets = [];
    let attributeNames = { "#thingId": thingName };
    let attributeValues = {};
    let start = "things.#thingId.devices.";
    
    var shadowIndex = 0;
    console.log(shadows)
    for (const [shadowId, shadow] of Object.entries(shadows)) {
        console.log(shadow);
        let shadowName = `#shadowVar_${shadowIndex}`;
        let shadowValue = `:shadowVal_${shadowIndex}`;
        attributeNames[shadowName] = shadowId;
        attributeValues[shadowValue] = shadow;
        let shadowSet = `${start}${shadowName} = ${shadowValue}`;
        sets.push(shadowSet);
        shadowIndex++;
    }
    console.log("initializationParamsDynamoDB");
    console.log(attributeNames);
    console.log(attributeValues);
    if (Object.keys(attributeValues).length === 0) {
        return false;
    }
    
    return {
        TableName: tableName,
        Key:{ "id": buildingId },  
        UpdateExpression: "set " + sets.join(", "),  
        ExpressionAttributeNames: attributeNames,
        ExpressionAttributeValues: attributeValues,
        ReturnValues:"UPDATED_NEW"
    };
}

function divideShadows(shadows, shadowsFound) {
    console.log("Diving Shadows")
    console.log(shadows);
    console.log(shadowsFound);
    let shadowsToUpdate = {};
    for (let shadowFound in shadowsFound) {
        console.log(shadowFound)
        if (shadows[shadowFound]) {
            let x = shadows[shadowFound]
            console.log(x)
            delete shadows[shadowFound];
            shadowsToUpdate[shadowFound] = x;
        }
    }
    
    console.log("Divided Shadows")
    console.log(shadows)
    console.log(shadowsToUpdate)
    return {
        "shadowsToCreate": shadows,
        "shadowsToUpdate": shadowsToUpdate
    };
}


function shadowsToDynamoMap(shadows) {
    let dynamoMap = {};
    console.log("dynamoMap")
    console.log(shadows)
    //console.log(Object.entries(shadows))
    for (const [shadowId, shadow] of Object.entries(shadows)) {
        let newShadow = {
            "friendly_name": shadowId,
            "exposes": shadow.exposes
        };
        //console.log(Object.entries(shadow.properties))
        let properties = shadow.properties
        for (const [propertyId, property] of Object.entries(properties)) {
            console.log(`propertyId: ${propertyId} and property: ${property}`)
            newShadow[propertyId] = property;
            console.log(newShadow)
        }
        dynamoMap[shadowId] = newShadow
    }
    console.log("finishedDynamoMap")
    return dynamoMap;
}
